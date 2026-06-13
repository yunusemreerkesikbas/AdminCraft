package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.application.commerce.dto.PaymentAttemptTotalsResponse;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class PaymentAttemptServiceImpl implements PaymentAttemptService {

	private static final int ATTEMPT_TTL_MINUTES = 30;
	private static final String PAYMENT_ENABLED_KEY = "commerce.payment.enabled";
	private static final String PAYMENT_PROVIDER_KEY = "commerce.payment.provider";
	private static final String DEFAULT_PROVIDER = "iyzico";

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCheckoutRepository checkoutRepository;
	private final CommercePaymentAttemptRepository paymentAttemptRepository;
	private final CommerceProductVariantLookupPort productVariantLookupPort;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;

	@Override
	@Transactional
	public PaymentAttemptResponse create(CommerceCustomerPrincipal principal, CreatePaymentAttemptCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		assertPaymentEnabled();
		String provider = resolveProvider();
		CommerceCheckout checkout = loadCheckout(principal, command);
		validateCheckoutReady(checkout);
		validateCheckoutStillPayable(checkout);

		paymentAttemptRepository.expirePendingAttemptsForCheckout(
				principal.customerId(),
				checkout.getId(),
				CommercePaymentAttemptStatus.PENDING);
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setCustomer(checkout.getCustomer());
		attempt.setCheckout(checkout);
		attempt.setProvider(provider);
		attempt.setStatus(CommercePaymentAttemptStatus.PENDING);
		attempt.setCurrencyIso(checkout.getCurrencyIso());
		attempt.setSubtotal(money(checkout.getSubtotal()));
		attempt.setVatTotal(money(checkout.getVatTotal()));
		attempt.setShippingTotal(money(checkout.getShippingTotal()));
		attempt.setTotal(money(checkout.getTotal()));
		attempt.setExpiresAt(LocalDateTime.now().plusMinutes(ATTEMPT_TTL_MINUTES));
		return toResponse(paymentAttemptRepository.save(attempt));
	}

	@Override
	@Transactional
	public PaymentAttemptResponse get(CommerceCustomerPrincipal principal, String attemptUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommercePaymentAttempt attempt = paymentAttemptRepository.findByCustomerIdAndUid(principal.customerId(), attemptUid)
				.orElseThrow(() -> new EntityNotFoundException("commerce.payment.attempt.not.found"));
		if (attempt.getStatus() == CommercePaymentAttemptStatus.PENDING && attemptNoLongerValid(attempt)) {
			attempt.setStatus(CommercePaymentAttemptStatus.EXPIRED);
			attempt = paymentAttemptRepository.save(attempt);
		}
		return toResponse(attempt);
	}

	private CommerceCheckout loadCheckout(CommerceCustomerPrincipal principal, CreatePaymentAttemptCommand command) {
		if (command == null || command.checkoutUid() == null || command.checkoutUid().isBlank()) {
			throw new IllegalArgumentException("commerce.payment.checkout.uid.required");
		}
		return checkoutRepository.findByCustomerIdAndUid(principal.customerId(), command.checkoutUid().trim())
				.orElseThrow(() -> new EntityNotFoundException("commerce.checkout.not.found"));
	}

	private void assertPaymentEnabled() {
		if (!configPropertyService.getBoolean(currentTenantId(), tenantContext.getTenantDbName(), PAYMENT_ENABLED_KEY, false)) {
			throw new IllegalStateException("commerce.payment.disabled");
		}
	}

	private String resolveProvider() {
		String provider = configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), PAYMENT_PROVIDER_KEY)
				.filter(value -> !value.isBlank())
				.map(value -> value.trim().toLowerCase())
				.orElse(DEFAULT_PROVIDER);
		if (!DEFAULT_PROVIDER.equals(provider)) {
			throw new IllegalArgumentException("commerce.payment.provider.unsupported");
		}
		return provider;
	}

	private void validateCheckoutReady(CommerceCheckout checkout) {
		if (checkout.getStatus() != CommerceCheckoutStatus.READY || checkoutExpired(checkout)) {
			throw new IllegalStateException("commerce.payment.checkout.not.ready");
		}
	}

	private void validateCheckoutStillPayable(CommerceCheckout checkout) {
		if (cartChanged(checkout)) {
			throw new CommerceDomainException("commerce.payment.checkout.changed");
		}
		Map<String, CommerceVariantSnapshot> variants = loadCheckoutVariants(checkout);
		if (priceChanged(checkout, variants)) {
			throw new CommerceDomainException("commerce.payment.checkout.price.changed");
		}
		if (stockChanged(checkout, variants)) {
			throw new CommerceDomainException("commerce.payment.checkout.stock.changed");
		}
	}

	private boolean attemptNoLongerValid(CommercePaymentAttempt attempt) {
		CommerceCheckout checkout = attempt.getCheckout();
		if (attempt.getExpiresAt() == null
				|| !attempt.getExpiresAt().isAfter(LocalDateTime.now())
				|| checkout == null
				|| checkout.getStatus() != CommerceCheckoutStatus.READY
				|| checkoutExpired(checkout)
				|| cartChanged(checkout)) {
			return true;
		}
		Map<String, CommerceVariantSnapshot> variants = loadCheckoutVariants(checkout);
		return priceChanged(checkout, variants) || stockChanged(checkout, variants);
	}

	private boolean checkoutExpired(CommerceCheckout checkout) {
		return checkout.getExpiresAt() == null || !checkout.getExpiresAt().isAfter(LocalDateTime.now());
	}

	private boolean cartChanged(CommerceCheckout checkout) {
		CommerceCart cart = checkout.getCart();
		if (cart == null
				|| cart.getStatus() != CommerceCartStatus.ACTIVE
				|| cart.getExpiresAt() == null
				|| !cart.getExpiresAt().isAfter(LocalDateTime.now())) {
			return true;
		}
		Map<String, Integer> cartQuantities = cart.getItems().stream()
				.collect(Collectors.toMap(
						CommerceCartItem::getVariantUid,
						item -> Objects.requireNonNullElse(item.getQuantity(), 0),
						Integer::sum));
		Map<String, Integer> checkoutQuantities = checkout.getItems().stream()
				.collect(Collectors.toMap(
						CommerceCheckoutItem::getVariantUid,
						item -> Objects.requireNonNullElse(item.getQuantity(), 0),
						Integer::sum));
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

	private PaymentAttemptResponse toResponse(CommercePaymentAttempt attempt) {
		return new PaymentAttemptResponse(
				attempt.getUid(),
				attempt.getCheckout().getUid(),
				attempt.getStatus().name(),
				attempt.getProvider(),
				attempt.getCurrencyIso(),
				new PaymentAttemptTotalsResponse(
						attempt.getCurrencyIso(),
						attempt.getSubtotal(),
						attempt.getVatTotal(),
						attempt.getShippingTotal(),
						attempt.getTotal()),
				attempt.getExpiresAt(),
				attempt.getFailureMessageKey());
	}

	private BigDecimal money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}
}
