package com.backend.application.commerce.dto;

import java.util.List;

public record CheckoutLegalResponse(
		boolean ready,
		String language,
		List<String> missingReasons,
		List<CommerceLegalDocumentResponse> documents) {
}
