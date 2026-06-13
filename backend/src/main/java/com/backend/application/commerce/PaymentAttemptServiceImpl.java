package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.CommercePaymentProviderPort.Address;
import com.backend.application.commerce.CommercePaymentProviderPort.BasketItem;
import com.backend.application.commerce.CommercePaymentProviderPort.Buyer;
import com.backend.application.commerce.CommercePaymentProviderPort.CheckoutFormInitializeCommand;
import com.backend.application.commerce.CommercePaymentProviderPort.CheckoutFormRetrieveCommand;
import com.backend.application.commerce.CommercePaymentProviderPort.CheckoutFormResult;
import com.backend.application.commerce.CommercePaymentProviderPort.Credentials;
import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CheckoutAddressSnapshotResponse;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.InitializePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.application.commerce.dto.PaymentAttemptTotalsResponse;
import com.backend.application.commerce.dto.PaymentInitializeResponse;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.TenantContextPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class PaymentAttemptServiceImpl implements PaymentAttemptService {

	private static final int ATTEMPT_TTL_MINUTES = 30;
	private static final String PAYMENT_ENABLED_KEY = "commerce.payment.enabled";
	private static final String PAYMENT_PROVIDER_KEY = "commerce.payment.provider";
	private static final String IYZICO_API_KEY = "commerce.payment.iyzico.api_key";
	private static final String IYZICO_SECRET_KEY = "commerce.payment.iyzico.secret_key";
	private static final String IYZICO_BASE_URL = "commerce.payment.iyzico.base_url";
	private static final String IYZICO_DEFAULT_IDENTITY_NUMBER = "commerce.payment.iyzico.default_identity_number";
	private static final String PAYMENT_RETURN_SUCCESS_URL = "commerce.payment.return_success_url";
	private static final String PAYMENT_RETURN_FAILURE_URL = "commerce.payment.return_failure_url";
	private static final String DEFAULT_PROVIDER = "iyzico";
	private static final String DEFAULT_IYZICO_BASE_URL = "https://sandbox-api.iyzipay.com";
	private static final String DEFAULT_COUNTRY = "Turkey";
	private static final String DEFAULT_CATEGORY = "Product";
	private static final String DEFAULT_CLIENT_IP = "127.0.0.1";

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCheckoutRepository checkoutRepository;
	private final CommercePaymentAttemptRepository paymentAttemptRepository;
	private final CommerceProductVariantLookupPort productVariantLookupPort;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final EncryptionServicePort encryptionService;
	private final ObjectMapper objectMapper;
	private final List<CommercePaymentProviderPort> paymentProviders;

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

	@Override
	@Transactional(noRollbackFor = CommercePaymentProviderException.class)
	public PaymentInitializeResponse initialize(CommerceCustomerPrincipal principal, InitializePaymentAttemptCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		assertPaymentEnabled();
		String providerCode = resolveProvider();
		CommercePaymentProviderPort provider = paymentProvider(providerCode);
		CommercePaymentAttempt attempt = loadAttempt(principal, command);
		validateAttemptCanInitialize(attempt);
		ProviderConfig config = loadProviderConfig();
		CheckoutFormInitializeCommand providerCommand = buildInitializeCommand(attempt, config, command);
		reserveInitialization(attempt);
		try {
			var result = provider.initializeCheckoutForm(providerCommand);
			if (result.token() == null || result.token().isBlank() || result.paymentPageUrl() == null || result.paymentPageUrl().isBlank()) {
				throw new CommercePaymentProviderException("commerce.payment.provider.initialize.failed");
			}
			attempt.setProviderReference(result.token().trim());
			attempt.setStatus(CommercePaymentAttemptStatus.PENDING);
			CommercePaymentAttempt saved = paymentAttemptRepository.save(attempt);
			return new PaymentInitializeResponse(
					saved.getUid(),
					saved.getStatus().name(),
					saved.getProvider(),
					result.paymentPageUrl());
		} catch (CommercePaymentProviderException ex) {
			markProviderFailure(attempt, null, "commerce.payment.provider.initialize.failed");
			throw ex;
		} catch (RuntimeException ex) {
			markProviderFailure(attempt, null, "commerce.payment.provider.initialize.failed");
			throw new CommercePaymentProviderException("commerce.payment.provider.initialize.failed", ex);
		}
	}

	@Override
	@Transactional
	public String handleIyzicoCheckoutFormCallback(String token) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		assertPaymentEnabled();
		String providerCode = resolveProvider();
		ProviderConfig config = loadProviderConfig();
		if (token == null || token.isBlank()) {
			return config.failureReturnUrl();
		}
		Optional<CommercePaymentAttempt> attemptOpt = paymentAttemptRepository
				.findFirstByProviderAndProviderReference(providerCode, token.trim());
		if (attemptOpt.isEmpty()) {
			return config.failureReturnUrl();
		}
		CommercePaymentAttempt attempt = attemptOpt.get();
		if (attempt.getStatus() != CommercePaymentAttemptStatus.PENDING) {
			return redirectUrlForStatus(attempt, config);
		}
		if (attemptNoLongerValid(attempt)) {
			attempt.setStatus(CommercePaymentAttemptStatus.EXPIRED);
			paymentAttemptRepository.save(attempt);
			return config.failureReturnUrl();
		}
		CheckoutFormResult result;
		try {
			result = paymentProvider(providerCode).retrieveCheckoutForm(
					new CheckoutFormRetrieveCommand(
							config.credentials(),
							attempt.getUid(),
							token.trim()));
		} catch (CommercePaymentProviderException ex) {
			markProviderFailure(attempt, "PROVIDER_RETRIEVE_FAILED", "commerce.payment.provider.retrieve.failed");
			return config.failureReturnUrl();
		}
		if (result.successful()) {
			attempt.setStatus(CommercePaymentAttemptStatus.SUCCEEDED);
			attempt.setProviderTransactionId(result.providerTransactionId());
			attempt.setFailureCode(null);
			attempt.setFailureMessageKey(null);
			paymentAttemptRepository.save(attempt);
			return config.successReturnUrl();
		}
		attempt.setStatus(CommercePaymentAttemptStatus.FAILED);
		attempt.setFailureCode(result.failureCode());
		attempt.setFailureMessageKey(nonBlankOrDefault(
				result.failureMessageKey(),
				"commerce.payment.provider.failed"));
		paymentAttemptRepository.save(attempt);
		return config.failureReturnUrl();
	}

	private CommercePaymentAttempt loadAttempt(CommerceCustomerPrincipal principal, InitializePaymentAttemptCommand command) {
		if (command == null || command.attemptUid() == null || command.attemptUid().isBlank()) {
			throw new IllegalArgumentException("commerce.payment.attempt.uid.required");
		}
		return paymentAttemptRepository.findByCustomerIdAndUid(principal.customerId(), command.attemptUid().trim())
				.orElseThrow(() -> new EntityNotFoundException("commerce.payment.attempt.not.found"));
	}

	private void validateAttemptCanInitialize(CommercePaymentAttempt attempt) {
		if (attempt.getStatus() != CommercePaymentAttemptStatus.PENDING) {
			throw new IllegalStateException("commerce.payment.attempt.not.pending");
		}
		if (attempt.getProviderReference() != null && !attempt.getProviderReference().isBlank()) {
			throw new IllegalStateException("commerce.payment.attempt.already.initialized");
		}
		if (attemptNoLongerValid(attempt)) {
			attempt.setStatus(CommercePaymentAttemptStatus.EXPIRED);
			paymentAttemptRepository.save(attempt);
			throw new IllegalStateException("commerce.payment.attempt.expired");
		}
		validateCheckoutReady(attempt.getCheckout());
		validateCheckoutStillPayable(attempt.getCheckout());
	}

	private void reserveInitialization(CommercePaymentAttempt attempt) {
		int reserved = paymentAttemptRepository.reservePendingAttemptInitialization(
				attempt.getId(),
				CommercePaymentAttemptStatus.PENDING,
				CommercePaymentAttemptStatus.INITIALIZING,
				LocalDateTime.now());
		if (reserved != 1) {
			throw new IllegalStateException("commerce.payment.attempt.already.initialized");
		}
		attempt.setStatus(CommercePaymentAttemptStatus.INITIALIZING);
	}

	private void markProviderFailure(CommercePaymentAttempt attempt, String failureCode, String failureMessageKey) {
		attempt.setStatus(CommercePaymentAttemptStatus.FAILED);
		attempt.setFailureCode(failureCode);
		attempt.setFailureMessageKey(failureMessageKey);
		paymentAttemptRepository.save(attempt);
	}

	private CheckoutFormInitializeCommand buildInitializeCommand(
			CommercePaymentAttempt attempt,
			ProviderConfig config,
			InitializePaymentAttemptCommand command) {
		CommerceCheckout checkout = attempt.getCheckout();
		CommerceCustomer customer = attempt.getCustomer();
		CheckoutAddressSnapshotResponse delivery = addressSnapshot(checkout.getDeliveryAddressSnapshot());
		CheckoutAddressSnapshotResponse billing = addressSnapshot(checkout.getBillingAddressSnapshot());
		return new CheckoutFormInitializeCommand(
				config.credentials(),
				attempt.getUid(),
				attempt.getUid(),
				checkout.getUid(),
				money(attempt.getSubtotal()),
				money(attempt.getTotal()),
				attempt.getCurrencyIso(),
				requiredUrl(command.callbackUrl(), "commerce.payment.callback.url.required"),
				buyer(customer, delivery, config.defaultIdentityNumber(), command.clientIp()),
				address(delivery),
				address(billing),
				basketItems(checkout));
	}

	private Buyer buyer(
			CommerceCustomer customer,
			CheckoutAddressSnapshotResponse address,
			String identityNumber,
			String clientIp) {
		return new Buyer(
				customer.getUid(),
				customer.getFirstName(),
				customer.getLastName(),
				customer.getPhone(),
				customer.getEmail(),
				identityNumber,
				nonBlankOrDefault(clientIp, DEFAULT_CLIENT_IP),
				fullAddress(address),
				address.city(),
				country(address.countryIso()),
				address.postalCode());
	}

	private Address address(CheckoutAddressSnapshotResponse snapshot) {
		return new Address(
				(snapshot.firstName() + " " + snapshot.lastName()).trim(),
				snapshot.city(),
				country(snapshot.countryIso()),
				fullAddress(snapshot),
				snapshot.postalCode());
	}

	private List<BasketItem> basketItems(CommerceCheckout checkout) {
		return checkout.getItems().stream()
				.map(item -> new BasketItem(
						nonBlankOrDefault(item.getUid(), item.getVariantUid()),
						(item.getProductSku() + " / " + item.getVariantSku()).trim(),
						DEFAULT_CATEGORY,
						money(item.getLineTotal())))
				.toList();
	}

	private String fullAddress(CheckoutAddressSnapshotResponse address) {
		return Stream.of(
						address.addressLine1(),
						address.addressLine2(),
						address.district(),
						address.city())
				.filter(value -> value != null && !value.isBlank())
				.collect(Collectors.joining(", "));
	}

	private CheckoutAddressSnapshotResponse addressSnapshot(String json) {
		try {
			return objectMapper.readValue(json, CheckoutAddressSnapshotResponse.class);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("commerce.checkout.address.snapshot.invalid", ex);
		}
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
				.map(value -> value.trim().toLowerCase(Locale.ROOT))
				.orElse(DEFAULT_PROVIDER);
		if (!DEFAULT_PROVIDER.equals(provider)) {
			throw new IllegalArgumentException("commerce.payment.provider.unsupported");
		}
		return provider;
	}

	private CommercePaymentProviderPort paymentProvider(String providerCode) {
		return paymentProviders.stream()
				.filter(provider -> provider.providerCode().equals(providerCode))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("commerce.payment.provider.unsupported"));
	}

	private ProviderConfig loadProviderConfig() {
		Credentials credentials = new Credentials(
				decryptedRequiredSecret(IYZICO_API_KEY),
				decryptedRequiredSecret(IYZICO_SECRET_KEY),
				requiredUrl(configRaw(IYZICO_BASE_URL).orElse(DEFAULT_IYZICO_BASE_URL), "commerce.payment.iyzico.base.url.invalid"));
		return new ProviderConfig(
				credentials,
				requiredIdentityNumber(configRaw(IYZICO_DEFAULT_IDENTITY_NUMBER)
						.orElseThrow(() -> new IllegalStateException("commerce.payment.iyzico.identity.required"))),
				requiredUrl(configRaw(PAYMENT_RETURN_SUCCESS_URL)
						.orElseThrow(() -> new IllegalStateException("commerce.payment.return.success.url.required")),
						"commerce.payment.return.success.url.invalid"),
				requiredUrl(configRaw(PAYMENT_RETURN_FAILURE_URL)
						.orElseThrow(() -> new IllegalStateException("commerce.payment.return.failure.url.required")),
						"commerce.payment.return.failure.url.invalid"));
	}

	private String decryptedRequiredSecret(String key) {
		ConfigProperty property = configPropertyService.find(currentTenantId(), tenantContext.getTenantDbName(), key)
				.orElseThrow(() -> new IllegalStateException("commerce.payment.config.required"));
		if (!Boolean.TRUE.equals(property.getSecret())) {
			throw new IllegalStateException("commerce.payment.config.secret.required");
		}
		String encrypted = property.getConfigValue();
		if (encrypted == null || encrypted.isBlank()) {
			throw new IllegalStateException("commerce.payment.config.required");
		}
		try {
			String decrypted = encryptionService.decrypt(encrypted);
			if (decrypted == null || decrypted.isBlank()) {
				throw new IllegalStateException("commerce.payment.config.required");
			}
			return decrypted.trim();
		} catch (RuntimeException ex) {
			throw new IllegalStateException("commerce.payment.config.invalid", ex);
		}
	}

	private Optional<String> configRaw(String key) {
		return configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), key)
				.map(String::trim)
				.filter(value -> !value.isBlank());
	}

	private String requiredIdentityNumber(String value) {
		String normalized = value.trim();
		if (!normalized.matches("\\d{10,11}")) {
			throw new IllegalStateException("commerce.payment.iyzico.identity.invalid");
		}
		return normalized;
	}

	private String requiredUrl(String value, String messageKey) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(messageKey);
		}
		try {
			URI uri = URI.create(value.trim());
			String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) || uri.getHost() == null) {
				throw new IllegalStateException(messageKey);
			}
			return uri.toString();
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException(messageKey, ex);
		}
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

	private String redirectUrlForStatus(CommercePaymentAttempt attempt, ProviderConfig config) {
		return attempt.getStatus() == CommercePaymentAttemptStatus.SUCCEEDED
				? config.successReturnUrl()
				: config.failureReturnUrl();
	}

	private String country(String countryIso) {
		return "TR".equalsIgnoreCase(countryIso) ? DEFAULT_COUNTRY : nonBlankOrDefault(countryIso, DEFAULT_COUNTRY);
	}

	private String nonBlankOrDefault(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value.trim();
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

	private record ProviderConfig(
			Credentials credentials,
			String defaultIdentityNumber,
			String successReturnUrl,
			String failureReturnUrl) {
	}
}
