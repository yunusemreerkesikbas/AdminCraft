package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.application.commerce.CommercePaymentProviderPort.CheckoutFormInitializeResult;
import com.backend.application.commerce.CommercePaymentProviderPort.CheckoutFormResult;
import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.InitializePaymentAttemptCommand;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class PaymentAttemptServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCheckoutRepository checkoutRepository;
	@Mock private CommercePaymentAttemptRepository paymentAttemptRepository;
	@Mock private CommerceOrderFinalizationService orderFinalizationService;
	@Mock private CommerceProductVariantLookupPort productVariantLookupPort;
	@Mock private CommerceLegalService commerceLegalService;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;
	@Mock private EncryptionServicePort encryptionService;
	@Mock private CommercePaymentProviderPort paymentProvider;
	@Mock private CommerceAdminNotificationService adminNotificationService;

	private PaymentAttemptServiceImpl service;

	@BeforeEach
	void setUp() {
		lenient().when(paymentProvider.providerCode()).thenReturn("iyzico");
		service = new PaymentAttemptServiceImpl(
				commerceModuleAccessGuard,
				checkoutRepository,
				paymentAttemptRepository,
				orderFinalizationService,
				productVariantLookupPort,
				commerceLegalService,
				configPropertyService,
				tenantContext,
				encryptionService,
				new ObjectMapper(),
				List.of(paymentProvider),
				adminNotificationService);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
		lenient().when(commerceLegalService.captureAcceptanceJson(any(), any(), any())).thenReturn("{}");
	}

	@Test
	void create_ShouldCreatePendingAttemptFromReadyCheckout() {
		stubPaymentEnabled();
		CommerceCheckout checkout = checkout();
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> {
			CommercePaymentAttempt attempt = invocation.getArgument(0);
			attempt.setUid("attempt-uid");
			return attempt;
		});

		var response = service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid"));

		assertThat(response.attemptUid()).isEqualTo("attempt-uid");
		assertThat(response.checkoutUid()).isEqualTo("checkout-uid");
		assertThat(response.status()).isEqualTo("PENDING");
		assertThat(response.provider()).isEqualTo("iyzico");
		assertThat(response.totals().total()).isEqualByComparingTo("200.00");
		assertThat(response.expiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
		verify(paymentAttemptRepository).expirePendingAttemptsForCheckout(
				10L,
				30L,
				CommercePaymentAttemptStatus.PENDING);
	}

	@Test
	void create_ShouldReject_WhenPaymentDisabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.payment.enabled", false)).thenReturn(false);

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.payment.disabled");
		verify(paymentAttemptRepository, never()).save(any());
	}

	@Test
	void create_ShouldReject_WhenCheckoutPriceChanged() {
		stubPaymentEnabled();
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout()));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(120), 5)));

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessageContaining("commerce.payment.checkout.price.changed");
		verify(paymentAttemptRepository, never()).save(any());
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenAttemptTtlExpired() {
		CommercePaymentAttempt attempt = attempt();
		attempt.setExpiresAt(LocalDateTime.now().minusMinutes(1));
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenCheckoutPriceChanged() {
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(120), 5)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenCheckoutStockChanged() {
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 1)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void initialize_ShouldInitializePendingAttemptAndStoreProviderToken() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(paymentProvider.initializeCheckoutForm(any()))
				.thenReturn(new CheckoutFormInitializeResult("provider-token", "https://sandbox-payment.example/pay"));
		when(paymentAttemptRepository.reservePendingAttemptInitialization(
				eq(40L),
				eq(CommercePaymentAttemptStatus.PENDING),
				eq(CommercePaymentAttemptStatus.INITIALIZING),
				any(LocalDateTime.class)))
				.thenReturn(1);
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1"));

		assertThat(response.paymentPageUrl()).isEqualTo("https://sandbox-payment.example/pay");
		assertThat(attempt.getProviderReference()).isEqualTo("provider-token");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.PENDING);
		verify(paymentProvider).initializeCheckoutForm(any());
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void initialize_ShouldReject_WhenConcurrentReserveFails() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(paymentAttemptRepository.reservePendingAttemptInitialization(
				eq(40L),
				eq(CommercePaymentAttemptStatus.PENDING),
				eq(CommercePaymentAttemptStatus.INITIALIZING),
				any(LocalDateTime.class)))
				.thenReturn(0);

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.payment.attempt.already.initialized");
		verify(paymentProvider, never()).initializeCheckoutForm(any());
	}

	@Test
	void initialize_ShouldMarkFailed_WhenProviderInitializeFailsAfterReserve() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(paymentAttemptRepository.reservePendingAttemptInitialization(
				eq(40L),
				eq(CommercePaymentAttemptStatus.PENDING),
				eq(CommercePaymentAttemptStatus.INITIALIZING),
				any(LocalDateTime.class)))
				.thenReturn(1);
		when(paymentProvider.initializeCheckoutForm(any()))
				.thenThrow(new CommercePaymentProviderException("commerce.payment.provider.initialize.failed"));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(CommercePaymentProviderException.class)
				.hasMessageContaining("commerce.payment.provider.initialize.failed");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.FAILED);
		assertThat(attempt.getFailureCode()).isEqualTo("PROVIDER_INITIALIZE_FAILED");
		assertThat(attempt.getFailureMessageKey()).isEqualTo("commerce.payment.provider.initialize.failed");
		verify(paymentAttemptRepository).save(attempt);
		verify(adminNotificationService).notifyPaymentOperationFailed(attempt, "PAYMENT_INITIALIZE");
	}

	@Test
	void initialize_ShouldReject_WhenAttemptAlreadyInitialized() {
		stubPaymentEnabled();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.payment.attempt.already.initialized");
		verify(paymentProvider, never()).initializeCheckoutForm(any());
	}

	@Test
	void initialize_ShouldReject_WhenApiKeyIsNotSecretConfig() {
		stubPaymentEnabled();
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(configPropertyService.find(1L, "tenant_1", "commerce.payment.iyzico.api_key"))
				.thenReturn(Optional.of(config("plain-api-key", false)));

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.payment.config.secret.required");
		verify(paymentProvider, never()).initializeCheckoutForm(any());
	}

	@Test
	void initialize_ShouldReject_WhenDeliveryAddressSnapshotIsNull() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.getCheckout().setDeliveryAddressSnapshot(null);
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.checkout.address.snapshot.invalid");
		verify(paymentProvider, never()).initializeCheckoutForm(any());
	}

	@Test
	void initialize_ShouldReject_WhenBillingAddressSnapshotIsBlank() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.getCheckout().setBillingAddressSnapshot(" ");
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));

		assertThatThrownBy(() -> service.initialize(
				principal(),
				new InitializePaymentAttemptCommand(
						"attempt-uid",
						"https://api.example.com/commerce/payments/iyzico/checkout-form/callback",
						"10.0.0.1")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.checkout.address.snapshot.invalid");
		verify(paymentProvider, never()).initializeCheckoutForm(any());
	}

	@Test
	void callback_ShouldMarkAttemptSucceededAndReturnSuccessUrl() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));
		when(paymentProvider.retrieveCheckoutForm(any()))
				.thenReturn(new CheckoutFormResult(true, "payment-123", null, null));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/success?paymentStatus=SUCCEEDED&attemptUid=attempt-uid");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.SUCCEEDED);
		assertThat(attempt.getProviderTransactionId()).isEqualTo("payment-123");
		verify(paymentAttemptRepository).save(attempt);
		verify(orderFinalizationService).finalizeSuccessfulPayment(attempt);
	}

	@Test
	void callback_ShouldAppendReturnQueryParamsAndPreserveExistingSuccessQuery() {
		stubPaymentEnabled();
		stubProviderConfig();
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.return_success_url"))
				.thenReturn(Optional.of("https://storefront.example.com/payment/success?locale=tr"));
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		CommerceOrder order = new CommerceOrder();
		order.setUid("order-uid");
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));
		when(paymentProvider.retrieveCheckoutForm(any()))
				.thenReturn(new CheckoutFormResult(true, "payment-123", null, null));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(orderFinalizationService.finalizeSuccessfulPayment(any())).thenReturn(order);

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl)
				.isEqualTo("https://storefront.example.com/payment/success?locale=tr&paymentStatus=SUCCEEDED&attemptUid=attempt-uid&orderUid=order-uid");
	}

	@Test
	void callback_ShouldFinalizeSuccessfulProviderResult_WhenLocalCheckoutNoLongerValid() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		attempt.getCheckout().getCart().setStatus(CommerceCartStatus.CLEARED);
		attempt.getCheckout().setExpiresAt(LocalDateTime.now().minusMinutes(1));
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));
		when(paymentProvider.retrieveCheckoutForm(any()))
				.thenReturn(new CheckoutFormResult(true, "payment-123", null, null));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/success?paymentStatus=SUCCEEDED&attemptUid=attempt-uid");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.SUCCEEDED);
		verify(paymentProvider).retrieveCheckoutForm(any());
		verify(orderFinalizationService).finalizeSuccessfulPayment(attempt);
	}

	@Test
	void callback_ShouldMarkAttemptFailed_WhenProviderResultFails() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));
		when(paymentProvider.retrieveCheckoutForm(any()))
				.thenReturn(new CheckoutFormResult(false, null, "NOT_SUFFICIENT_FUNDS", "commerce.payment.provider.failed"));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/failure?paymentStatus=FAILED&attemptUid=attempt-uid");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.FAILED);
		assertThat(attempt.getFailureCode()).isEqualTo("NOT_SUFFICIENT_FUNDS");
		assertThat(attempt.getFailureMessageKey()).isEqualTo("commerce.payment.provider.failed");
	}

	@Test
	void callback_ShouldMarkAttemptFailed_WhenProviderRetrieveThrows() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));
		when(paymentProvider.retrieveCheckoutForm(any()))
				.thenThrow(new CommercePaymentProviderException("commerce.payment.provider.retrieve.failed"));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/failure?paymentStatus=FAILED&attemptUid=attempt-uid");
		assertThat(attempt.getStatus()).isEqualTo(CommercePaymentAttemptStatus.FAILED);
		assertThat(attempt.getFailureCode()).isEqualTo("PROVIDER_RETRIEVE_FAILED");
		assertThat(attempt.getFailureMessageKey()).isEqualTo("commerce.payment.provider.retrieve.failed");
		verify(paymentAttemptRepository).save(attempt);
		verify(adminNotificationService).notifyPaymentOperationFailed(attempt, "PAYMENT_RETRIEVE");
	}

	@Test
	void callback_ShouldNotOverwriteTerminalStatus() {
		stubPaymentEnabled();
		stubProviderConfig();
		CommercePaymentAttempt attempt = attempt();
		attempt.setProviderReference("provider-token");
		attempt.setStatus(CommercePaymentAttemptStatus.SUCCEEDED);
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "provider-token"))
				.thenReturn(Optional.of(attempt));

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("provider-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/success?paymentStatus=SUCCEEDED&attemptUid=attempt-uid");
		verify(paymentProvider, never()).retrieveCheckoutForm(any());
		verify(paymentAttemptRepository, never()).save(any());
		verify(orderFinalizationService).finalizeSuccessfulPayment(attempt);
	}

	@Test
	void callback_ShouldReturnFailureUrl_WhenTokenUnknown() {
		stubPaymentEnabled();
		stubProviderConfig();
		when(paymentAttemptRepository.findFirstByProviderAndProviderReference("iyzico", "unknown-token"))
				.thenReturn(Optional.empty());

		String redirectUrl = service.handleIyzicoCheckoutFormCallback("unknown-token");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/failure?paymentStatus=FAILED");
		verify(paymentProvider, never()).retrieveCheckoutForm(any());
	}

	@Test
	void callback_ShouldReturnFailureUrl_WhenTokenIsNull() {
		stubPaymentEnabled();
		stubProviderConfig();

		String redirectUrl = service.handleIyzicoCheckoutFormCallback(null);

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/failure?paymentStatus=FAILED");
		verify(paymentProvider, never()).retrieveCheckoutForm(any());
	}

	@Test
	void callback_ShouldReturnFailureUrl_WhenTokenIsBlank() {
		stubPaymentEnabled();
		stubProviderConfig();

		String redirectUrl = service.handleIyzicoCheckoutFormCallback(" ");

		assertThat(redirectUrl).isEqualTo("https://storefront.example.com/payment/failure?paymentStatus=FAILED");
		verify(paymentProvider, never()).retrieveCheckoutForm(any());
	}

	@Test
	void create_ShouldReject_WhenCommerceDisabled() {
		doThrow(new IllegalStateException("commerce.module.not.enabled"))
				.when(commerceModuleAccessGuard).assertEnabledForCurrentTenant();

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.module.not.enabled");
	}

	private void stubPaymentEnabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.payment.enabled", false)).thenReturn(true);
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.provider")).thenReturn(Optional.empty());
	}

	private void stubProviderConfig() {
		when(configPropertyService.find(1L, "tenant_1", "commerce.payment.iyzico.api_key"))
				.thenReturn(Optional.of(config("encrypted-api-key", true)));
		when(configPropertyService.find(1L, "tenant_1", "commerce.payment.iyzico.secret_key"))
				.thenReturn(Optional.of(config("encrypted-secret-key", true)));
		when(encryptionService.decrypt("encrypted-api-key")).thenReturn("sandbox-api-key");
		when(encryptionService.decrypt("encrypted-secret-key")).thenReturn("sandbox-secret-key");
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.iyzico.base_url"))
				.thenReturn(Optional.empty());
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.iyzico.default_identity_number"))
				.thenReturn(Optional.of("11111111110"));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.return_success_url"))
				.thenReturn(Optional.of("https://storefront.example.com/payment/success"));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.return_failure_url"))
				.thenReturn(Optional.of("https://storefront.example.com/payment/failure"));
	}

	private ConfigProperty config(String value, boolean secret) {
		ConfigProperty property = new ConfigProperty();
		property.setConfigValue(value);
		property.setSecret(secret);
		return property;
	}

	private CommercePaymentAttempt attempt() {
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setId(40L);
		attempt.setUid("attempt-uid");
		attempt.setCustomer(customer());
		attempt.setCheckout(checkout());
		attempt.setProvider("iyzico");
		attempt.setStatus(CommercePaymentAttemptStatus.PENDING);
		attempt.setCurrencyIso("TRY");
		attempt.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setVatTotal(BigDecimal.valueOf(33.33));
		attempt.setShippingTotal(BigDecimal.ZERO.setScale(2));
		attempt.setTotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		return attempt;
	}

	private CommerceCheckout checkout() {
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		checkout.setCustomer(customer());
		checkout.setCart(activeCart());
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCurrencyIso("TRY");
		checkout.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setVatTotal(BigDecimal.valueOf(33.33));
		checkout.setShippingTotal(BigDecimal.ZERO.setScale(2));
		checkout.setTotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setExpiresAt(LocalDateTime.now().plusHours(1));
		checkout.setDeliveryAddressSnapshot(addressSnapshot());
		checkout.setBillingAddressSnapshot(addressSnapshot());
		checkout.addItem(checkoutItem());
		return checkout;
	}

	private String addressSnapshot() {
		return """
				{
				  "uid": "address-uid",
				  "label": "Home",
				  "firstName": "Jane",
				  "lastName": "Doe",
				  "phone": "+905350000000",
				  "countryIso": "TR",
				  "city": "Istanbul",
				  "district": "Kadikoy",
				  "addressLine1": "Test Street 1",
				  "addressLine2": null,
				  "postalCode": "34710",
				  "invoiceType": "INDIVIDUAL",
				  "companyName": null,
				  "taxNumber": null,
				  "taxOffice": null,
				  "invoiceIdentityNumber": null
				}
				""";
	}

	private CommerceCheckoutItem checkoutItem() {
		CommerceCheckoutItem item = new CommerceCheckoutItem();
		item.setUid("checkout-item-uid");
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid("variant-uid");
		item.setVariantSku("VAR-1");
		item.setQuantity(2);
		item.setUnitGrossPrice(BigDecimal.valueOf(100).setScale(2));
		item.setVatRate(BigDecimal.valueOf(20).setScale(2));
		item.setLineTotal(BigDecimal.valueOf(200).setScale(2));
		item.setLineVatTotal(BigDecimal.valueOf(33.33));
		return item;
	}

	private CommerceCart activeCart() {
		CommerceCart cart = new CommerceCart();
		cart.setId(20L);
		cart.setUid("cart-uid");
		cart.setCustomer(customer());
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(1));
		cart.addItem(cartItem());
		return cart;
	}

	private CommerceCartItem cartItem() {
		CommerceCartItem item = new CommerceCartItem();
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid("variant-uid");
		item.setVariantSku("VAR-1");
		item.setQuantity(2);
		item.setUnitGrossPrice(BigDecimal.valueOf(100));
		item.setVatRate(BigDecimal.valueOf(20));
		return item;
	}

	private CommerceVariantSnapshot variant(BigDecimal price, int stockQuantity) {
		return new CommerceVariantSnapshot(
				"product-uid",
				"PROD-1",
				true,
				true,
				"variant-uid",
				"VAR-1",
				true,
				price,
				BigDecimal.valueOf(20),
				stockQuantity);
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setPhone("+905350000000");
		return customer;
	}

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "user@example.com", 1L);
	}
}
