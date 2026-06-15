package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceOrder;

public record CommerceOrderSummaryResponse(
		String orderUid,
		String orderNumber,
		String status,
		LocalDateTime createdAt,
		String currencyIso,
		CheckoutTotalsResponse totals,
		int itemCount,
		Boolean requiresAttention,
		String attentionReasonKey) {

	public static CommerceOrderSummaryResponse from(CommerceOrder order) {
		return from(order, order.getItems() == null ? 0 : order.getItems().size());
	}

	public static CommerceOrderSummaryResponse from(CommerceOrder order, int itemCount) {
		return new CommerceOrderSummaryResponse(
				order.getUid(),
				order.getOrderNumber(),
				order.getStatus().name(),
				order.getCreatedAt(),
				order.getCurrencyIso(),
				new CheckoutTotalsResponse(
						order.getCurrencyIso(),
						order.getSubtotal(),
						order.getVatTotal(),
						order.getShippingTotal(),
						order.getTotal()),
				itemCount,
				order.getRequiresAttention(),
				order.getAttentionReasonKey());
	}
}
