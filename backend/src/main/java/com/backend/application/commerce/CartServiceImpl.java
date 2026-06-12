package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.domain.commerce.CommerceCartLimits.MAX_QUANTITY;
import static com.backend.domain.commerce.CommerceCartLimits.MIN_QUANTITY;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CartItemResponse;
import com.backend.application.commerce.dto.CartResponse;
import com.backend.application.commerce.dto.CartTotalsResponse;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.enums.Currency;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.commerce.repository.CommerceCartItemRepository;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int CART_TTL_DAYS = 30;
    private static final String CART_NOT_FOUND = "Cart";

    private final CommerceModuleAccessGuard commerceModuleAccessGuard;
    private final CommerceCartRepository cartRepository;
    private final CommerceCartItemRepository cartItemRepository;
	private final CommerceCustomerRepository customerRepository;
    private final CommerceProductVariantLookupPort productVariantLookupPort;
    private final CartTokenService cartTokenService;

    @Override
    @Transactional
    public CartResponse createCart() {
		return createCart(null);
	}

	@Override
	@Transactional
	public CartResponse createCart(CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomer customer = principal == null ? null : lockCustomer(principal);
		if (customer != null) {
			Optional<CommerceCart> existingCart = findActiveCustomerCart(customer.getId());
			if (existingCart.isPresent()) {
				return toResponse(existingCart.get(), null);
			}
		}
		CreatedCart createdCart = createActiveCart(customer, principal == null);
		CommerceCart saved = cartRepository.save(createdCart.cart());
		return toResponse(saved, createdCart.rawToken());
	}

	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(String cartToken) {
		return getCart(cartToken, null);
	}

	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(String cartToken, CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		if (principal != null) {
			CommerceCart cart = loadActiveCustomerCart(principal);
			return toResponse(cart, null);
		}
		CommerceCart cart = loadActiveAnonymousCart(cartToken);
		return toResponse(cart, cartToken);
	}

	@Override
	@Transactional
	public CartResponse addItem(String cartToken, String variantUid, Integer quantity) {
		return addItem(cartToken, null, variantUid, quantity);
	}

	@Override
	@Transactional
	public CartResponse addItem(String cartToken, CommerceCustomerPrincipal principal, String variantUid, Integer quantity) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		int requestedQuantity = validateQuantity(quantity);
		CommerceVariantSnapshot variant = loadVariant(variantUid);
		validateSellable(variant);

		String responseToken = cartToken;
		CommerceCart cart;
		if (principal != null) {
			cart = loadOrCreateActiveCustomerCart(principal);
			responseToken = null;
		} else if (cartToken == null || cartToken.isBlank()) {
			CreatedCart createdCart = createActiveCart(null, true);
			cart = createdCart.cart();
			responseToken = createdCart.rawToken();
		} else {
			cart = loadActiveAnonymousCart(cartToken);
		}

		Optional<CommerceCartItem> existingItem = cart.getItems().stream()
				.filter(item -> Objects.equals(item.getVariantUid(), variant.variantUid()))
				.findFirst();
		int finalQuantity = existingItem
				.map(item -> item.getQuantity() + requestedQuantity)
				.orElse(requestedQuantity);
		validateQuantity(finalQuantity);
		validateStock(variant, finalQuantity);

		if (existingItem.isPresent()) {
			existingItem.get().setQuantity(finalQuantity);
		} else {
			CommerceCartItem item = new CommerceCartItem();
			item.setProductUid(variant.productUid());
			item.setProductSku(variant.productSku());
			item.setVariantUid(variant.variantUid());
			item.setVariantSku(variant.variantSku());
			item.setQuantity(finalQuantity);
			item.setUnitGrossPrice(variant.price());
			item.setVatRate(variant.vatRate());
			cart.addItem(item);
		}

		CommerceCart saved = cartRepository.save(cart);
		return toResponse(saved, responseToken);
	}

	@Override
	@Transactional
	public CartResponse updateItem(String cartToken, String itemUid, Integer quantity) {
		return updateItem(cartToken, null, itemUid, quantity);
	}

	@Override
	@Transactional
	public CartResponse updateItem(String cartToken, CommerceCustomerPrincipal principal, String itemUid, Integer quantity) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		int requestedQuantity = validateQuantity(quantity);
		CommerceCart cart = principal == null ? loadActiveAnonymousCart(cartToken) : loadActiveCustomerCart(principal);
		CommerceCartItem item = loadCartItem(cart, itemUid);
		CommerceVariantSnapshot variant = loadVariant(item.getVariantUid());
		validateSellable(variant);
		validateStock(variant, requestedQuantity);
		item.setQuantity(requestedQuantity);
		CommerceCart saved = cartRepository.save(cart);
		return toResponse(saved, principal == null ? cartToken : null);
	}

	@Override
	@Transactional
	public CartResponse deleteItem(String cartToken, String itemUid) {
		return deleteItem(cartToken, null, itemUid);
	}

	@Override
	@Transactional
	public CartResponse deleteItem(String cartToken, CommerceCustomerPrincipal principal, String itemUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCart cart = principal == null ? loadActiveAnonymousCart(cartToken) : loadActiveCustomerCart(principal);
		CommerceCartItem item = loadCartItem(cart, itemUid);
		cart.getItems().remove(item);
		CommerceCart saved = cartRepository.save(cart);
		return toResponse(saved, principal == null ? cartToken : null);
	}

	@Override
	@Transactional
	public void clearCart(String cartToken) {
		clearCart(cartToken, null);
	}

	@Override
	@Transactional
	public void clearCart(String cartToken, CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCart cart = principal == null ? loadActiveAnonymousCart(cartToken) : loadActiveCustomerCart(principal);
		cart.getItems().clear();
		cart.setStatus(CommerceCartStatus.CLEARED);
		cartRepository.save(cart);
	}

	private CreatedCart createActiveCart(CommerceCustomer customer, boolean exposeRawToken) {
		String rawToken = cartTokenService.generateToken();
		CommerceCart cart = new CommerceCart();
		cart.setTokenHash(cartTokenService.hashToken(rawToken));
		if (customer != null) {
			cart.setCustomer(customer);
		}
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(CART_TTL_DAYS));
		return new CreatedCart(cart, exposeRawToken ? rawToken : null);
	}

	private CommerceCart loadActiveAnonymousCart(String cartToken) {
		if (cartToken == null || cartToken.isBlank()) {
			throw new IllegalArgumentException("commerce.cart.token.required");
		}
		String tokenHash = cartTokenService.hashToken(cartToken);
		CommerceCart cart = cartRepository.findByTokenHashAndStatus(tokenHash, CommerceCartStatus.ACTIVE)
				.orElseThrow(() -> new EntityNotFoundException(CART_NOT_FOUND, "token"));
		if (cart.getCustomer() != null || isExpired(cart)) {
			throw new EntityNotFoundException(CART_NOT_FOUND, "expired token");
		}
		return cart;
	}

	private CommerceCart loadActiveCustomerCart(CommerceCustomerPrincipal principal) {
		return findActiveCustomerCart(principal)
				.orElseThrow(() -> new EntityNotFoundException(CART_NOT_FOUND, "customer"));
	}

	private CommerceCart loadOrCreateActiveCustomerCart(CommerceCustomerPrincipal principal) {
		CommerceCustomer customer = lockCustomer(principal);
		return findActiveCustomerCart(customer.getId())
				.orElseGet(() -> createActiveCart(customer, false).cart());
	}

	private Optional<CommerceCart> findActiveCustomerCart(CommerceCustomerPrincipal principal) {
		return findActiveCustomerCart(principal.customerId());
	}

	private Optional<CommerceCart> findActiveCustomerCart(Long customerId) {
		return cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				customerId,
				CommerceCartStatus.ACTIVE,
				LocalDateTime.now());
	}

	private CommerceCustomer lockCustomer(CommerceCustomerPrincipal principal) {
		return customerRepository.findByIdForUpdate(principal.customerId())
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.not.found"));
	}

	private boolean isExpired(CommerceCart cart) {
		return cart.getExpiresAt() == null || cart.getExpiresAt().isBefore(LocalDateTime.now());
	}

    private CommerceCartItem loadCartItem(CommerceCart cart, String itemUid) {
        if (itemUid == null || itemUid.isBlank()) {
            throw new EntityNotFoundException("Cart item", "missing uid");
        }
        return cartItemRepository.findByCartIdAndUid(cart.getId(), itemUid)
                .orElseThrow(() -> new EntityNotFoundException("Cart item", itemUid));
    }

    private CommerceVariantSnapshot loadVariant(String variantUid) {
        if (variantUid == null || variantUid.isBlank()) {
            throw new EntityNotFoundException("Product variant", "missing uid");
        }
        return productVariantLookupPort.findByVariantUid(variantUid)
                .orElseThrow(() -> new EntityNotFoundException("Product variant", variantUid));
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new CommerceDomainException("commerce.cart.quantity.invalid");
        }
        return quantity;
    }

    private void validateSellable(CommerceVariantSnapshot variant) {
        if (!variant.sellable()) {
            throw new CommerceDomainException("commerce.cart.variant.not.sellable");
        }
    }

    private void validateStock(CommerceVariantSnapshot variant, int quantity) {
        int stockQuantity = variant.stockQuantity() == null ? 0 : variant.stockQuantity();
        if (stockQuantity < quantity) {
            throw new CommerceDomainException("commerce.cart.stock.insufficient");
        }
    }

    private CartResponse toResponse(CommerceCart cart, String rawToken) {
		Map<String, CommerceVariantSnapshot> currentVariants = Optional.ofNullable(productVariantLookupPort.findByVariantUids(
				cart.getItems().stream()
						.map(CommerceCartItem::getVariantUid)
						.filter(Objects::nonNull)
						.collect(Collectors.toSet())))
				.orElse(Map.of());
        List<CartItemResponse> items = cart.getItems().stream()
				.map(item -> toItemResponse(item, Optional.ofNullable(currentVariants.get(item.getVariantUid()))))
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vatTotal = cart.getItems().stream()
                .map(this::calculateVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream()
                .map(CartItemResponse::quantity)
                .reduce(0, Integer::sum);
		Map<String, CommerceCartItem> itemsByVariantUid = cart.getItems().stream()
				.collect(Collectors.toMap(CommerceCartItem::getVariantUid, Function.identity(), (left, right) -> left));
		BigDecimal currentTotal = currentVariants.values().stream()
				.filter(variant -> itemsByVariantUid.containsKey(variant.variantUid()))
				.map(variant -> calculateLineTotal(
						variant.price(),
						itemsByVariantUid.get(variant.variantUid()).getQuantity()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal currentVatTotal = currentVariants.values().stream()
				.filter(variant -> itemsByVariantUid.containsKey(variant.variantUid()))
				.map(variant -> calculateVatAmount(
						variant.price(),
						variant.vatRate(),
						itemsByVariantUid.get(variant.variantUid()).getQuantity()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
        CartTotalsResponse totals = new CartTotalsResponse(
                Currency.getDefault().getIsoCode(),
                itemCount,
                total,
                vatTotal,
				total,
				currentVatTotal,
				currentTotal);
        return new CartResponse(
                rawToken,
                cart.getUid(),
                cart.getStatus().name(),
                cart.getExpiresAt(),
                items,
                totals);
    }

    private CartItemResponse toItemResponse(CommerceCartItem item, Optional<CommerceVariantSnapshot> currentVariant) {
        BigDecimal currentPrice = currentVariant.map(CommerceVariantSnapshot::price).orElse(null);
        BigDecimal currentVatRate = currentVariant.map(CommerceVariantSnapshot::vatRate).orElse(item.getVatRate());
        Integer stockQuantity = currentVariant.map(CommerceVariantSnapshot::stockQuantity).orElse(null);
        boolean priceChanged = currentPrice != null
                && (currentPrice.compareTo(item.getUnitGrossPrice()) != 0
                        || currentVatRate.compareTo(item.getVatRate()) != 0);
        boolean available = currentVariant
                .map(variant -> variant.sellable()
                        && (variant.stockQuantity() == null ? 0 : variant.stockQuantity()) >= item.getQuantity())
                .orElse(false);
		BigDecimal lineTotal = calculateLineTotal(item.getUnitGrossPrice(), item.getQuantity());
        return new CartItemResponse(
                item.getUid(),
                item.getProductUid(),
                item.getProductSku(),
                item.getVariantUid(),
                item.getVariantSku(),
                item.getQuantity(),
                item.getUnitGrossPrice(),
                currentPrice,
                item.getVatRate(),
                lineTotal,
                priceChanged,
                available,
                stockQuantity);
    }

    private BigDecimal calculateVatAmount(CommerceCartItem item) {
		return calculateVatAmount(item.getUnitGrossPrice(), item.getVatRate(), item.getQuantity());
    }

    private BigDecimal calculateVatAmount(BigDecimal unitGrossPrice, BigDecimal vatRate, int quantity) {
		BigDecimal lineTotal = unitGrossPrice.multiply(BigDecimal.valueOf(quantity));
		BigDecimal denominator = BigDecimal.valueOf(100).add(vatRate);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
		return lineTotal.multiply(vatRate)
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLineTotal(BigDecimal unitGrossPrice, int quantity) {
		return unitGrossPrice
				.multiply(BigDecimal.valueOf(quantity))
				.setScale(2, RoundingMode.HALF_UP);
    }

    private record CreatedCart(CommerceCart cart, String rawToken) {
    }
}
