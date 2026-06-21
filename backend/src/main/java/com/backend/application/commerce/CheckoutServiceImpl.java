package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CheckoutAddressSelectionCommand;
import com.backend.application.commerce.dto.CheckoutAddressSnapshotResponse;
import com.backend.application.commerce.dto.CheckoutItemResponse;
import com.backend.application.commerce.dto.CheckoutResponse;
import com.backend.application.commerce.dto.CheckoutShippingResponse;
import com.backend.application.commerce.dto.CheckoutTotalsResponse;
import com.backend.application.commerce.dto.CheckoutValidationResponse;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerAddress;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommerceCustomerAddressRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.enums.Currency;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CheckoutServiceImpl implements CheckoutService {

	private static final int CHECKOUT_TTL_HOURS = 24;
	private static final String SHIPPING_ENABLED_KEY = "commerce.shipping.enabled";
	private static final String SHIPPING_STANDARD_FEE_KEY = "commerce.shipping.standard_fee";
	private static final String SHIPPING_FREE_THRESHOLD_KEY = "commerce.shipping.free_shipping_threshold";
	private static final String STANDARD_SHIPPING_CODE = "STANDARD";
	private static final String STANDARD_SHIPPING_NAME_KEY = "commerce.shipping.method.standard";
	private static final List<CommerceCheckoutStatus> OPEN_STATUSES = List.of(
			CommerceCheckoutStatus.DRAFT,
			CommerceCheckoutStatus.READY);

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCartRepository cartRepository;
	private final CommerceCheckoutRepository checkoutRepository;
	private final CommerceCustomerRepository customerRepository;
	private final CommerceCustomerAddressRepository addressRepository;
	private final CommerceProductVariantLookupPort productVariantLookupPort;
	private final CommerceLegalService commerceLegalService;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public CheckoutResponse start(CommerceCustomerPrincipal principal, CheckoutAddressSelectionCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		LocalDateTime now = LocalDateTime.now();
		CommerceCustomer customer = loadCustomer(principal);
		CommerceCart cart = loadActiveCustomerCart(principal, now);
		validateCart(cart);
		Map<String, CommerceVariantSnapshot> variants = loadVariants(cart.getItems());
		AddressSelection addresses = resolveAddresses(principal.customerId(), command);
		CheckoutDraft draft = buildDraft(cart, variants, addresses, now);

		checkoutRepository.expireOpenCheckouts(principal.customerId(), OPEN_STATUSES, now);
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setCustomer(customer);
		checkout.setCart(cart);
		applyDraft(checkout, draft);
		CommerceCheckout saved = checkoutRepository.save(checkout);
		return toResponse(saved, validation(saved));
	}

	@Override
	@Transactional(readOnly = true)
	public CheckoutResponse getCurrent(CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCheckout checkout = checkoutRepository
				.findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(
						principal.customerId(),
						OPEN_STATUSES,
						LocalDateTime.now())
				.orElseThrow(() -> new EntityNotFoundException("commerce.checkout.not.found"));
		return toResponse(checkout, validation(checkout));
	}

	@Override
	@Transactional
	public CheckoutResponse updateAddresses(
			CommerceCustomerPrincipal principal,
			String checkoutUid,
			CheckoutAddressSelectionCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCheckout checkout = checkoutRepository.findByCustomerIdAndUid(principal.customerId(), checkoutUid)
				.orElseThrow(() -> new EntityNotFoundException("commerce.checkout.not.found"));
		if (!OPEN_STATUSES.contains(checkout.getStatus()) || isExpired(checkout)) {
			throw new IllegalStateException("commerce.checkout.expired");
		}
		AddressSelection addresses = resolveAddresses(principal.customerId(), command);
		checkout.setDeliveryAddressUid(addresses.delivery().getUid());
		checkout.setBillingAddressUid(addresses.billing().getUid());
		checkout.setDeliveryAddressSnapshot(toJson(toAddressSnapshot(addresses.delivery())));
		checkout.setBillingAddressSnapshot(toJson(toAddressSnapshot(addresses.billing())));
		CheckoutTotals totals = calculateTotals(checkout.getItems(), shippingTotal(checkout.getSubtotal()));
		checkout.setShippingTotal(totals.shippingTotal());
		checkout.setTotal(totals.total());
		CommerceCheckout saved = checkoutRepository.save(checkout);
		return toResponse(saved, validation(saved));
	}

	private CommerceCustomer loadCustomer(CommerceCustomerPrincipal principal) {
		return customerRepository.findById(principal.customerId())
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.not.found"));
	}

	private CommerceCart loadActiveCustomerCart(CommerceCustomerPrincipal principal, LocalDateTime now) {
		return cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
						principal.customerId(),
						CommerceCartStatus.ACTIVE,
						now)
				.orElseThrow(() -> new EntityNotFoundException("commerce.checkout.cart.not.found"));
	}

	private void validateCart(CommerceCart cart) {
		if (cart.getItems() == null || cart.getItems().isEmpty()) {
			throw new CommerceDomainException("commerce.checkout.cart.empty");
		}
	}

	private Map<String, CommerceVariantSnapshot> loadVariants(Collection<CommerceCartItem> items) {
		Set<String> variantUids = items.stream()
				.map(CommerceCartItem::getVariantUid)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (variantUids.isEmpty()) {
			return Map.of();
		}
		return Optional.ofNullable(productVariantLookupPort.findByVariantUids(variantUids))
				.orElse(Map.of());
	}

	private AddressSelection resolveAddresses(Long customerId, CheckoutAddressSelectionCommand command) {
		CheckoutAddressSelectionCommand safeCommand = command != null
				? command
				: new CheckoutAddressSelectionCommand(null, null, null);
		CommerceCustomerAddress delivery = resolveDeliveryAddress(customerId, safeCommand.deliveryAddressUid());
		CommerceCustomerAddress billing = Boolean.TRUE.equals(safeCommand.billingSameAsDelivery())
				? delivery
				: resolveBillingAddress(customerId, safeCommand.billingAddressUid());
		return new AddressSelection(delivery, billing);
	}

	private CommerceCustomerAddress resolveDeliveryAddress(Long customerId, String uid) {
		if (uid != null && !uid.isBlank()) {
			return addressRepository.findByCustomerIdAndUid(customerId, uid.trim())
					.orElseThrow(() -> new IllegalArgumentException("commerce.checkout.delivery.address.required"));
		}
		return addressRepository.findFirstByCustomerIdAndDefaultDeliveryTrueOrderByIdAsc(customerId)
				.orElseThrow(() -> new IllegalArgumentException("commerce.checkout.delivery.address.required"));
	}

	private CommerceCustomerAddress resolveBillingAddress(Long customerId, String uid) {
		if (uid != null && !uid.isBlank()) {
			return addressRepository.findByCustomerIdAndUid(customerId, uid.trim())
					.orElseThrow(() -> new IllegalArgumentException("commerce.checkout.billing.address.required"));
		}
		return addressRepository.findFirstByCustomerIdAndDefaultBillingTrueOrderByIdAsc(customerId)
				.orElseThrow(() -> new IllegalArgumentException("commerce.checkout.billing.address.required"));
	}

	private CheckoutDraft buildDraft(
			CommerceCart cart,
			Map<String, CommerceVariantSnapshot> variants,
			AddressSelection addresses,
			LocalDateTime now) {
		List<CommerceCheckoutItem> items = new ArrayList<>();
		for (CommerceCartItem cartItem : cart.getItems()) {
			CommerceVariantSnapshot variant = variants.get(cartItem.getVariantUid());
			validateVariantForCheckout(variant, cartItem.getQuantity());
			CommerceCheckoutItem item = new CommerceCheckoutItem();
			item.setProductUid(variant.productUid());
			item.setProductSku(variant.productSku());
			item.setVariantUid(variant.variantUid());
			item.setVariantSku(variant.variantSku());
			item.setQuantity(cartItem.getQuantity());
			item.setUnitGrossPrice(money(variant.price()));
			item.setVatRate(money(variant.vatRate()));
			item.setLineTotal(calculateLineTotal(variant.price(), cartItem.getQuantity()));
			item.setLineVatTotal(calculateVatAmount(variant.price(), variant.vatRate(), cartItem.getQuantity()));
			items.add(item);
		}
		BigDecimal subtotal = items.stream()
				.map(CommerceCheckoutItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		CheckoutTotals totals = calculateTotals(items, shippingTotal(subtotal));
		return new CheckoutDraft(
				items,
				totals,
				addresses,
				now.plusHours(CHECKOUT_TTL_HOURS));
	}

	private void validateVariantForCheckout(CommerceVariantSnapshot variant, int quantity) {
		if (variant == null || !variant.sellable()) {
			throw new CommerceDomainException("commerce.checkout.variant.not.sellable");
		}
		if (variant.price() == null || variant.vatRate() == null) {
			throw new CommerceDomainException("commerce.checkout.variant.not.sellable");
		}
		int stockQuantity = Objects.requireNonNullElse(variant.stockQuantity(), 0);
		if (stockQuantity < quantity) {
			throw new CommerceDomainException("commerce.checkout.stock.insufficient");
		}
	}

	private CheckoutTotals calculateTotals(Collection<CommerceCheckoutItem> items, BigDecimal shippingTotal) {
		BigDecimal subtotal = items.stream()
				.map(CommerceCheckoutItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal vatTotal = items.stream()
				.map(CommerceCheckoutItem::getLineVatTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal safeShipping = money(shippingTotal);
		return new CheckoutTotals(
				money(subtotal),
				money(vatTotal),
				safeShipping,
				money(subtotal.add(safeShipping)));
	}

	private void applyDraft(CommerceCheckout checkout, CheckoutDraft draft) {
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCurrencyIso(currencyIso());
		checkout.setSubtotal(draft.totals().subtotal());
		checkout.setVatTotal(draft.totals().vatTotal());
		checkout.setShippingTotal(draft.totals().shippingTotal());
		checkout.setTotal(draft.totals().total());
		checkout.setShippingMethodCode(STANDARD_SHIPPING_CODE);
		checkout.setShippingMethodName(STANDARD_SHIPPING_NAME_KEY);
		checkout.setDeliveryAddressUid(draft.addresses().delivery().getUid());
		checkout.setBillingAddressUid(draft.addresses().billing().getUid());
		checkout.setDeliveryAddressSnapshot(toJson(toAddressSnapshot(draft.addresses().delivery())));
		checkout.setBillingAddressSnapshot(toJson(toAddressSnapshot(draft.addresses().billing())));
		checkout.setExpiresAt(draft.expiresAt());
		checkout.getItems().clear();
		draft.items().forEach(checkout::addItem);
	}

	private CheckoutValidationResponse validation(CommerceCheckout checkout) {
		Set<String> warningKeys = new LinkedHashSet<>();
		boolean cartChanged = cartChanged(checkout);
		if (cartChanged) {
			warningKeys.add("commerce.checkout.cart.changed");
		}
		Map<String, CommerceVariantSnapshot> variants = loadCheckoutVariants(checkout);
		boolean priceChanged = priceChanged(checkout, variants);
		if (priceChanged) {
			warningKeys.add("commerce.checkout.price.changed");
		}
		boolean stockChanged = stockChanged(checkout, variants);
		if (stockChanged) {
			warningKeys.add("commerce.checkout.stock.changed");
		}
		boolean valid = !cartChanged && !priceChanged && !stockChanged;
		return new CheckoutValidationResponse(
				valid,
				cartChanged,
				priceChanged,
				stockChanged,
				List.copyOf(warningKeys));
	}

	private boolean cartChanged(CommerceCheckout checkout) {
		CommerceCart cart = checkout.getCart();
		if (cart == null || cart.getStatus() != CommerceCartStatus.ACTIVE || cart.getExpiresAt() == null || cart.getExpiresAt().isBefore(LocalDateTime.now())) {
			return true;
		}
		Map<String, Integer> cartQuantities = cart.getItems().stream()
				.collect(Collectors.toMap(CommerceCartItem::getVariantUid, CommerceCartItem::getQuantity, Integer::sum));
		Map<String, Integer> checkoutQuantities = checkout.getItems().stream()
				.collect(Collectors.toMap(CommerceCheckoutItem::getVariantUid, CommerceCheckoutItem::getQuantity, Integer::sum));
		return !cartQuantities.equals(checkoutQuantities);
	}

	private Map<String, CommerceVariantSnapshot> loadCheckoutVariants(CommerceCheckout checkout) {
		Set<String> variantUids = checkout.getItems().stream()
				.map(CommerceCheckoutItem::getVariantUid)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (variantUids.isEmpty()) {
			return Map.of();
		}
		return Optional.ofNullable(productVariantLookupPort.findByVariantUids(variantUids))
				.orElse(Map.of());
	}

	private boolean priceChanged(CommerceCheckout checkout, Map<String, CommerceVariantSnapshot> variants) {
		return checkout.getItems().stream().anyMatch(item -> {
			CommerceVariantSnapshot variant = variants.get(item.getVariantUid());
			return variant == null
					|| variant.price() == null
					|| variant.vatRate() == null
					|| variant.price().compareTo(item.getUnitGrossPrice()) != 0
					|| variant.vatRate().compareTo(item.getVatRate()) != 0;
		});
	}

	private boolean stockChanged(CommerceCheckout checkout, Map<String, CommerceVariantSnapshot> variants) {
		return checkout.getItems().stream().anyMatch(item -> {
			CommerceVariantSnapshot variant = variants.get(item.getVariantUid());
			if (variant == null || !variant.sellable()) {
				return true;
			}
			return Objects.requireNonNullElse(variant.stockQuantity(), 0) < item.getQuantity();
		});
	}

	private CheckoutResponse toResponse(CommerceCheckout checkout, CheckoutValidationResponse validation) {
		List<CheckoutItemResponse> items = checkout.getItems().stream()
				.map(item -> new CheckoutItemResponse(
						item.getUid(),
						item.getProductUid(),
						item.getProductSku(),
						item.getVariantUid(),
						item.getVariantSku(),
						item.getQuantity(),
						item.getUnitGrossPrice(),
						item.getVatRate(),
						item.getLineTotal(),
						item.getLineVatTotal()))
				.toList();
		return new CheckoutResponse(
				checkout.getUid(),
				checkout.getStatus().name(),
				checkout.getExpiresAt(),
				fromJson(checkout.getDeliveryAddressSnapshot()),
				fromJson(checkout.getBillingAddressSnapshot()),
				items,
				new CheckoutTotalsResponse(
						checkout.getCurrencyIso(),
						checkout.getSubtotal(),
						checkout.getVatTotal(),
						checkout.getShippingTotal(),
						checkout.getTotal()),
				new CheckoutShippingResponse(
						checkout.getShippingMethodCode(),
						checkout.getShippingMethodName(),
						checkout.getShippingTotal()),
				validation,
				commerceLegalService.legalForCheckout(checkout, checkoutLanguage()));
	}

	private String checkoutLanguage() {
		return LocaleContextHolder.getLocale().getLanguage();
	}

	private CheckoutAddressSnapshotResponse toAddressSnapshot(CommerceCustomerAddress address) {
		return new CheckoutAddressSnapshotResponse(
				address.getUid(),
				address.getLabel(),
				address.getFirstName(),
				address.getLastName(),
				address.getPhone(),
				address.getCountryIso(),
				address.getCity(),
				address.getDistrict(),
				address.getAddressLine1(),
				address.getAddressLine2(),
				address.getPostalCode(),
				address.getInvoiceType().name(),
				address.getCompanyName(),
				address.getTaxNumber(),
				address.getTaxOffice(),
				address.getInvoiceIdentityNumber());
	}

	private BigDecimal shippingTotal(BigDecimal subtotal) {
		if (!shippingEnabled()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal fee = nonNegative(configDecimal(SHIPPING_STANDARD_FEE_KEY, BigDecimal.ZERO));
		Optional<BigDecimal> freeThreshold = optionalConfigDecimal(SHIPPING_FREE_THRESHOLD_KEY);
		if (freeThreshold.isPresent() && subtotal.compareTo(freeThreshold.get()) >= 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return fee;
	}

	private boolean shippingEnabled() {
		return configPropertyService.getBoolean(currentTenantId(), tenantContext.getTenantDbName(), SHIPPING_ENABLED_KEY, true);
	}

	private BigDecimal configDecimal(String key, BigDecimal defaultValue) {
		return configPropertyService.getDecimal(currentTenantId(), tenantContext.getTenantDbName(), key, defaultValue);
	}

	private Optional<BigDecimal> optionalConfigDecimal(String key) {
		return configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), key)
				.filter(value -> !value.isBlank())
				.flatMap(value -> {
					try {
						BigDecimal parsed = new BigDecimal(value.trim());
						return parsed.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(parsed) : Optional.empty();
					} catch (NumberFormatException ex) {
						return Optional.empty();
					}
				});
	}

	private BigDecimal nonNegative(BigDecimal value) {
		if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return money(value);
	}

	private String currencyIso() {
		Currency currency = tenantContext.getCurrency();
		return currency != null ? currency.getIsoCode() : Currency.getDefault().getIsoCode();
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}

	private boolean isExpired(CommerceCheckout checkout) {
		return checkout.getExpiresAt() == null || checkout.getExpiresAt().isBefore(LocalDateTime.now());
	}

	private String toJson(CheckoutAddressSnapshotResponse snapshot) {
		try {
			return objectMapper.writeValueAsString(snapshot);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("commerce.checkout.address.snapshot.invalid", ex);
		}
	}

	private CheckoutAddressSnapshotResponse fromJson(String json) {
		try {
			return objectMapper.readValue(json, CheckoutAddressSnapshotResponse.class);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("commerce.checkout.address.snapshot.invalid", ex);
		}
	}

	private BigDecimal calculateLineTotal(BigDecimal unitGrossPrice, int quantity) {
		return money(unitGrossPrice).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateVatAmount(BigDecimal unitGrossPrice, BigDecimal vatRate, int quantity) {
		BigDecimal lineTotal = calculateLineTotal(unitGrossPrice, quantity);
		BigDecimal denominator = BigDecimal.valueOf(100).add(money(vatRate));
		if (denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return lineTotal.multiply(money(vatRate))
				.divide(denominator, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private record AddressSelection(CommerceCustomerAddress delivery, CommerceCustomerAddress billing) {
	}

	private record CheckoutDraft(
			List<CommerceCheckoutItem> items,
			CheckoutTotals totals,
			AddressSelection addresses,
			LocalDateTime expiresAt) {
	}

	private record CheckoutTotals(
			BigDecimal subtotal,
			BigDecimal vatTotal,
			BigDecimal shippingTotal,
			BigDecimal total) {
	}
}
