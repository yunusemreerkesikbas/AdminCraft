package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.TestingAuthenticationToken;

import com.backend.application.commerce.CheckoutService;
import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.dto.CheckoutResponse;
import com.backend.application.commerce.dto.CheckoutLegalResponse;
import com.backend.application.commerce.dto.CheckoutShippingResponse;
import com.backend.application.commerce.dto.CheckoutTotalsResponse;
import com.backend.application.commerce.dto.CheckoutValidationResponse;

@ExtendWith(MockitoExtension.class)
class CommerceCheckoutControllerTest {

	@Mock private CheckoutService checkoutService;
	@Mock private MessageSource messageSource;

	@Test
	void start_ShouldReturnShippingMethodNameKeyAndLocalizedApiMessage() {
		CommerceCheckoutController controller = new CommerceCheckoutController(checkoutService, messageSource);
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
		when(checkoutService.start(any(), any())).thenReturn(checkoutResponse());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
					case "commerce.checkout.started" -> "Checkout started";
					default -> invocation.getArgument(2);
				});

		var response = controller.start(authentication, null);

		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("Checkout started");
		assertThat(response.getBody().getData().shipping().methodNameKey()).isEqualTo("commerce.shipping.method.standard");
	}

	private CheckoutResponse checkoutResponse() {
		return new CheckoutResponse(
				"checkout-uid",
				"READY",
				LocalDateTime.now().plusHours(1),
				null,
				null,
				List.of(),
				new CheckoutTotalsResponse("TRY", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
				new CheckoutShippingResponse("STANDARD", "commerce.shipping.method.standard", BigDecimal.ZERO),
				new CheckoutValidationResponse(true, false, false, false, List.of()),
				new CheckoutLegalResponse(true, "EN", List.of(), List.of()));
	}
}
