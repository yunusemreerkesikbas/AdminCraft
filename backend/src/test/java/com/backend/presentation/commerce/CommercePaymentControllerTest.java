package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommercePaymentProperties;
import com.backend.application.commerce.PaymentAttemptService;
import com.backend.application.commerce.dto.InitializePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.application.commerce.dto.PaymentAttemptTotalsResponse;
import com.backend.application.commerce.dto.PaymentInitializeResponse;
import com.backend.domain.port.TenantContextPort;
import com.backend.infrastructure.config.AppSecurityProperties;

@ExtendWith(MockitoExtension.class)
class CommercePaymentControllerTest {

	@Mock private PaymentAttemptService paymentAttemptService;
	@Mock private MessageSource messageSource;
	@Mock private TenantContextPort tenantContext;

	@Test
	void createAttempt_ShouldReturnLocalizedApiMessage() {
		CommercePaymentController controller = controller();
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
		when(paymentAttemptService.create(any(), any())).thenReturn(response());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Payment attempt created");

		var result = controller.createAttempt(authentication, new CreatePaymentAttemptRequest("checkout-uid"));

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Payment attempt created");
		assertThat(result.getBody().getData().attemptUid()).isEqualTo("attempt-uid");
	}

	@Test
	void initializeAttempt_ShouldReturnPaymentPageUrlAndBuildCallbackUrl() {
		AppSecurityProperties appSecurityProperties = new AppSecurityProperties();
		appSecurityProperties.setTrustedProxyCidrs(List.of("10.0.0.0/8"));
		CommercePaymentProperties paymentProperties = new CommercePaymentProperties();
		paymentProperties.setCallbackBaseUrl("https://%s.craftive.io/api");
		CommercePaymentController controller = controller(appSecurityProperties, paymentProperties);
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/commerce/payments/attempts/attempt-uid/initialize");
		request.setRemoteAddr("10.0.0.5");
		request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		when(tenantContext.getSubdomain()).thenReturn("tenant");
		when(paymentAttemptService.initialize(any(), any()))
				.thenReturn(new PaymentInitializeResponse("attempt-uid", "PENDING", "iyzico", "https://pay.example.com"));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Payment attempt initialized");

		try {
			var result = controller.initializeAttempt(authentication, "attempt-uid", request);

			assertThat(result.getBody()).isNotNull();
			assertThat(result.getBody().getMessage()).isEqualTo("Payment attempt initialized");
			assertThat(result.getBody().getData().paymentPageUrl()).isEqualTo("https://pay.example.com");
			ArgumentCaptor<InitializePaymentAttemptCommand> captor = ArgumentCaptor.forClass(InitializePaymentAttemptCommand.class);
			verify(paymentAttemptService).initialize(any(), captor.capture());
			assertThat(captor.getValue().callbackUrl())
					.isEqualTo("https://tenant.craftive.io/api/commerce/payments/iyzico/checkout-form/callback");
			assertThat(captor.getValue().clientIp()).isEqualTo("10.0.0.1");
		} finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}

	@Test
	void initializeAttempt_ShouldIgnoreForwardedFor_WhenRemoteAddrIsNotTrustedProxy() {
		AppSecurityProperties appSecurityProperties = new AppSecurityProperties();
		appSecurityProperties.setTrustedProxyCidrs(List.of("10.0.0.0/8"));
		CommercePaymentController controller = controller(appSecurityProperties, new CommercePaymentProperties());
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/commerce/payments/attempts/attempt-uid/initialize");
		request.setRemoteAddr("203.0.113.10");
		request.addHeader("X-Forwarded-For", "10.0.0.1");
		when(paymentAttemptService.initialize(any(), any()))
				.thenReturn(new PaymentInitializeResponse("attempt-uid", "PENDING", "iyzico", "https://pay.example.com"));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Payment attempt initialized");

		controller.initializeAttempt(authentication, "attempt-uid", request);

		ArgumentCaptor<InitializePaymentAttemptCommand> captor = ArgumentCaptor.forClass(InitializePaymentAttemptCommand.class);
		verify(paymentAttemptService).initialize(any(), captor.capture());
		assertThat(captor.getValue().clientIp()).isEqualTo("203.0.113.10");
	}

	@Test
	void initializeAttempt_ShouldFallbackToRemoteAddr_WhenForwardedForIsInvalid() {
		AppSecurityProperties appSecurityProperties = new AppSecurityProperties();
		appSecurityProperties.setTrustedProxyCidrs(List.of("10.0.0.0/8"));
		CommercePaymentController controller = controller(appSecurityProperties, new CommercePaymentProperties());
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/commerce/payments/attempts/attempt-uid/initialize");
		request.setRemoteAddr("10.0.0.5");
		request.addHeader("X-Forwarded-For", "not-an-ip");
		when(paymentAttemptService.initialize(any(), any()))
				.thenReturn(new PaymentInitializeResponse("attempt-uid", "PENDING", "iyzico", "https://pay.example.com"));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Payment attempt initialized");

		controller.initializeAttempt(authentication, "attempt-uid", request);

		ArgumentCaptor<InitializePaymentAttemptCommand> captor = ArgumentCaptor.forClass(InitializePaymentAttemptCommand.class);
		verify(paymentAttemptService).initialize(any(), captor.capture());
		assertThat(captor.getValue().clientIp()).isEqualTo("10.0.0.5");
	}

	@Test
	void iyzicoCheckoutFormCallback_ShouldRedirectToServiceUrl() {
		CommercePaymentController controller = controller();
		when(paymentAttemptService.handleIyzicoCheckoutFormCallback("provider-token"))
				.thenReturn("https://storefront.example.com/payment/success");

		var result = controller.iyzicoCheckoutFormCallback("provider-token");

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(result.getHeaders().getLocation()).hasToString("https://storefront.example.com/payment/success");
	}

	private CommercePaymentController controller() {
		return controller(new AppSecurityProperties(), new CommercePaymentProperties());
	}

	private CommercePaymentController controller(
			AppSecurityProperties appSecurityProperties,
			CommercePaymentProperties paymentProperties) {
		return new CommercePaymentController(
				paymentAttemptService,
				messageSource,
				tenantContext,
				appSecurityProperties,
				paymentProperties);
	}

	private PaymentAttemptResponse response() {
		return new PaymentAttemptResponse(
				"attempt-uid",
				"checkout-uid",
				"PENDING",
				"iyzico",
				"TRY",
				new PaymentAttemptTotalsResponse("TRY", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN),
				LocalDateTime.now().plusMinutes(30),
				null);
	}
}
