package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceOrderResolutionRequest;

public record CustomerOrderResolutionRequestResponse(
		String requestUid,
		String orderUid,
		String orderNumber,
		String type,
		String status,
		String reason,
		String description,
		String requestedOrderStatus,
		String refundStatus,
		LocalDateTime createdAt,
		LocalDateTime decidedAt,
		LocalDateTime refundedAt) {

	public static CustomerOrderResolutionRequestResponse from(CommerceOrderResolutionRequest request) {
		return new CustomerOrderResolutionRequestResponse(
				request.getUid(),
				request.getOrder().getUid(),
				request.getOrder().getOrderNumber(),
				request.getType().name(),
				request.getStatus().name(),
				request.getReason(),
				request.getDescription(),
				request.getRequestedOrderStatus().name(),
				request.getRefundStatus().name(),
				request.getCreatedAt(),
				request.getDecidedAt(),
				request.getRefundedAt());
	}
}
