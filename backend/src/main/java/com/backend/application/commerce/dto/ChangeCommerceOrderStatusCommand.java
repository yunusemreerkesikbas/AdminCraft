package com.backend.application.commerce.dto;

import com.backend.domain.commerce.CommerceOrderStatus;

public record ChangeCommerceOrderStatusCommand(
		CommerceOrderStatus status,
		String carrierName,
		String trackingNumber,
		String trackingUrl,
		String internalNote) {
}
