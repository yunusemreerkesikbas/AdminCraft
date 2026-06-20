package com.backend.presentation.commerce;

import java.util.List;

import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentAttemptRequest(
		@NotBlank(message = "{commerce.payment.checkout.uid.required}") @Uid String checkoutUid,
		List<@NotNull(message = "{commerce.legal.acceptance.required}") @Valid LegalAcceptanceRequest> legalAcceptances) {

	public record LegalAcceptanceRequest(
			@NotBlank(message = "{commerce.legal.template.uid.required}") @Uid String templateUid,
			@NotNull(message = "{commerce.legal.acceptance.required}") Integer version,
			@NotNull(message = "{commerce.legal.acceptance.required}") Boolean accepted) {
	}
}
