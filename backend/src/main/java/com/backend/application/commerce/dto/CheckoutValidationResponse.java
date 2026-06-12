package com.backend.application.commerce.dto;

import java.util.List;

public record CheckoutValidationResponse(
		boolean valid,
		boolean cartChanged,
		boolean priceChanged,
		boolean stockChanged,
		List<String> warningMessageKeys) {
}
