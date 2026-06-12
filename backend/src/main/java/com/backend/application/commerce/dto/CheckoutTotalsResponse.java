package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CheckoutTotalsResponse(
		String currencyIso,
		BigDecimal subtotal,
		BigDecimal vatTotal,
		BigDecimal shippingTotal,
		BigDecimal total) {
}
