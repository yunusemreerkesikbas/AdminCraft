package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;

public record CommerceAdminOrderSummaryResponse(
		Long id,
		String orderUid,
		String orderNumber,
		String customerUid,
		String customerName,
		String customerEmail,
		String status,
		LocalDateTime createdAt,
		String currencyIso,
		CheckoutTotalsResponse totals,
		int itemCount,
		String provider,
		Boolean requiresAttention,
		String attentionReasonKey) {

	public static CommerceAdminOrderSummaryResponse from(CommerceOrder order, int itemCount) {
		CommerceCustomer customer = order.getCustomer();
		return new CommerceAdminOrderSummaryResponse(
				order.getId(),
				order.getUid(),
				order.getOrderNumber(),
				customer.getUid(),
				customer.getFirstName() + " " + customer.getLastName(),
				customer.getEmail(),
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
				order.getProvider(),
				order.isRequiresAttention(),
				order.getAttentionReasonKey());
	}
}
