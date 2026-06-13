package com.backend.presentation.commerce;

import com.backend.shared.validation.Uid;

import jakarta.validation.constraints.NotBlank;

public record CreatePaymentAttemptRequest(
		@NotBlank(message = "{commerce.payment.checkout.uid.required}") @Uid String checkoutUid) {
}
