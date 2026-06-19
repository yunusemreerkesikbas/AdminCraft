package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceOrder;

public record CommerceOrderFulfillmentResponse(
		String carrierName,
		String trackingNumber,
		String trackingUrl,
		LocalDateTime shippedAt,
		LocalDateTime deliveredAt,
		LocalDateTime statusChangedAt) {

	public static CommerceOrderFulfillmentResponse from(CommerceOrder order) {
		return new CommerceOrderFulfillmentResponse(
				order.getShippingCarrierName(),
				order.getShippingTrackingNumber(),
				order.getShippingTrackingUrl(),
				order.getShippedAt(),
				order.getDeliveredAt(),
				order.getStatusChangedAt());
	}
}
