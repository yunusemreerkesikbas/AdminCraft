package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CheckoutShippingResponse(
		String methodCode,
		String methodNameKey,
		BigDecimal amount) {
}
