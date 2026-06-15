package com.backend.application.commerce.dto;

import java.math.BigDecimal;

import com.backend.domain.commerce.CommerceOrderItem;

public record CommerceOrderItemResponse(
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

	public static CommerceOrderItemResponse from(CommerceOrderItem item) {
		return new CommerceOrderItemResponse(
				item.getUid(),
				item.getProductUid(),
				item.getProductSku(),
				item.getVariantUid(),
				item.getVariantSku(),
				item.getQuantity(),
				item.getUnitGrossPrice(),
				item.getVatRate(),
				item.getLineTotal(),
				item.getLineVatTotal());
	}
}
