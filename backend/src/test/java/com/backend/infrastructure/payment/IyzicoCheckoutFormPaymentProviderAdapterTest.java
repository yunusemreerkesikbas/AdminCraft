package com.backend.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.backend.application.commerce.CommercePaymentProviderException;
import com.iyzipay.model.CheckoutFormInitialize;

class IyzicoCheckoutFormPaymentProviderAdapterTest {

	private final IyzicoCheckoutFormPaymentProviderAdapter adapter = new IyzicoCheckoutFormPaymentProviderAdapter();

	@Test
	void toResult_ShouldReject_WhenTokenIsBlank() {
		CheckoutFormInitialize initialize = mock(CheckoutFormInitialize.class);
		when(initialize.getToken()).thenReturn(" ");
		when(initialize.getPaymentPageUrl()).thenReturn("https://pay.example.com");

		assertThatThrownBy(() -> adapter.toResult(initialize))
				.isInstanceOf(CommercePaymentProviderException.class)
				.hasMessageContaining("commerce.payment.provider.initialize.failed");
	}

	@Test
	void toResult_ShouldReject_WhenPaymentPageUrlIsBlank() {
		CheckoutFormInitialize initialize = mock(CheckoutFormInitialize.class);
		when(initialize.getToken()).thenReturn("provider-token");
		when(initialize.getPaymentPageUrl()).thenReturn(" ");

		assertThatThrownBy(() -> adapter.toResult(initialize))
				.isInstanceOf(CommercePaymentProviderException.class)
				.hasMessageContaining("commerce.payment.provider.initialize.failed");
	}

	@Test
	void toResult_ShouldTrimProviderValues() {
		CheckoutFormInitialize initialize = mock(CheckoutFormInitialize.class);
		when(initialize.getToken()).thenReturn(" provider-token ");
		when(initialize.getPaymentPageUrl()).thenReturn(" https://pay.example.com ");

		var result = adapter.toResult(initialize);

		assertThat(result.token()).isEqualTo("provider-token");
		assertThat(result.paymentPageUrl()).isEqualTo("https://pay.example.com");
	}
}
