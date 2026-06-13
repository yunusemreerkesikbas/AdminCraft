package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record PaymentAttemptTotalsResponse(
		String currencyIso,
		BigDecimal subtotal,
		BigDecimal vatTotal,
		BigDecimal shippingTotal,
		BigDecimal total) {
}
