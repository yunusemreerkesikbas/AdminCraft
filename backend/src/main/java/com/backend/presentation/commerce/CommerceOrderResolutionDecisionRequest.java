package com.backend.presentation.commerce;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommerceOrderResolutionDecisionRequest(
		@NotNull(message = "commerce.order.request.decision.required")
		Decision decision,

		@Size(max = 1000, message = "commerce.order.request.decisionNote.length")
		String decisionNote) {

	public enum Decision {
		APPROVE,
		REJECT
	}
}
