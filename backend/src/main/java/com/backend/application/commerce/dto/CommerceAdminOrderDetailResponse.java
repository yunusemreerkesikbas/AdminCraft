package com.backend.application.commerce.dto;

import java.util.List;

import com.backend.domain.commerce.CommerceOrder;

public record CommerceAdminOrderDetailResponse(
		CommerceAdminOrderSummaryResponse summary,
		String customerPhone,
		String providerTransactionId,
		String legalSnapshotStatus,
		String legalSnapshotJson,
		Boolean stockDeducted,
		List<CommerceOrderItemResponse> items,
		CheckoutShippingResponse shipping,
		CommerceOrderFulfillmentResponse fulfillment,
		CheckoutAddressSnapshotResponse deliveryAddress,
		CheckoutAddressSnapshotResponse billingAddress,
		CommerceAdminOrderPaymentResponse paymentAttempt,
		List<CommerceOrderStatusHistoryResponse> statusHistory) {

	public static CommerceAdminOrderDetailResponse from(
			CommerceOrder order,
			CheckoutAddressSnapshotResponse deliveryAddress,
			CheckoutAddressSnapshotResponse billingAddress) {
		List<CommerceOrderItemResponse> items = order.getItems().stream()
				.map(CommerceOrderItemResponse::from)
				.toList();
		List<CommerceOrderStatusHistoryResponse> history = order.getStatusHistory().stream()
				.map(CommerceOrderStatusHistoryResponse::from)
				.toList();
		return new CommerceAdminOrderDetailResponse(
				CommerceAdminOrderSummaryResponse.from(order, items.size()),
				order.getCustomer().getPhone(),
				order.getProviderTransactionId(),
				order.getLegalSnapshotStatus().name(),
				order.getLegalSnapshotJson(),
				order.isStockDeducted(),
				items,
				new CheckoutShippingResponse(
						order.getShippingMethodCode(),
						order.getShippingMethodName(),
						order.getShippingTotal()),
				CommerceOrderFulfillmentResponse.from(order),
				deliveryAddress,
				billingAddress,
				CommerceAdminOrderPaymentResponse.from(order.getPaymentAttempt()),
				history);
	}
}
