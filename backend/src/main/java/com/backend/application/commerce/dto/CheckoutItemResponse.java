package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CheckoutItemResponse(
		String uid,
		String productUid,
		String productSku,
		String variantUid,
		String variantSku,
		Integer quantity,
		BigDecimal unitGrossPrice,
		BigDecimal vatRate,
		BigDecimal lineTotal,
		BigDecimal lineVatTotal) {
}
