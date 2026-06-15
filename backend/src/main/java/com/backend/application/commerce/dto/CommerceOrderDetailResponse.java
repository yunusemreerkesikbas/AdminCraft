package com.backend.application.commerce.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.commerce.CommerceOrder;

public record CommerceOrderDetailResponse(
		String orderUid,
		String orderNumber,
		String status,
		LocalDateTime createdAt,
		String currencyIso,
		CheckoutTotalsResponse totals,
		int itemCount,
		Boolean requiresAttention,
		String attentionReasonKey,
		List<CommerceOrderItemResponse> items,
		CheckoutShippingResponse shipping,
		CheckoutAddressSnapshotResponse deliveryAddress,
		CheckoutAddressSnapshotResponse billingAddress,
		String legalSnapshotStatus) {

	public static CommerceOrderDetailResponse from(
			CommerceOrder order,
			CheckoutAddressSnapshotResponse deliveryAddress,
			CheckoutAddressSnapshotResponse billingAddress) {
		List<CommerceOrderItemResponse> items = order.getItems().stream()
				.map(CommerceOrderItemResponse::from)
				.toList();
		return new CommerceOrderDetailResponse(
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
				items.size(),
				order.getRequiresAttention(),
				order.getAttentionReasonKey(),
				items,
				new CheckoutShippingResponse(
						order.getShippingMethodCode(),
						order.getShippingMethodName(),
						order.getShippingTotal()),
				deliveryAddress,
				billingAddress,
				order.getLegalSnapshotStatus().name());
	}
}
