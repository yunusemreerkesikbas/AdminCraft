package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.TestingAuthenticationToken;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.PaymentAttemptService;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.application.commerce.dto.PaymentAttemptTotalsResponse;

@ExtendWith(MockitoExtension.class)
class CommercePaymentControllerTest {

	@Mock private PaymentAttemptService paymentAttemptService;
	@Mock private MessageSource messageSource;

	@Test
	void createAttempt_ShouldReturnLocalizedApiMessage() {
		CommercePaymentController controller = new CommercePaymentController(paymentAttemptService, messageSource);
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
