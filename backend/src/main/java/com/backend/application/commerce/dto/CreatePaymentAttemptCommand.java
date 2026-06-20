package com.backend.application.commerce.dto;

import java.util.List;

public record CreatePaymentAttemptCommand(
		String checkoutUid,
		List<CommerceLegalAcceptanceCommand> legalAcceptances) {

	public CreatePaymentAttemptCommand(String checkoutUid) {
		this(checkoutUid, List.of());
	}
}
