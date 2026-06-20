package com.backend.application.commerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceOrderResolutionRequest;

public record CommerceOrderResolutionRequestResponse(
		Long id,
		String requestUid,
		String orderUid,
		String orderNumber,
		String customerEmail,
		String type,
		String status,
		String reason,
		String description,
		String previousOrderStatus,
		String requestedOrderStatus,
		String decisionNote,
		Long decidedByUserId,
		String decidedByEmail,
		String refundStatus,
		String refundProvider,
		String refundReference,
		String refundFailureCode,
		String refundFailureMessageKey,
		Boolean stockRestored,
		BigDecimal orderTotal,
		String currencyIso,
		LocalDateTime createdAt,
		LocalDateTime decidedAt,
		LocalDateTime refundAttemptedAt,
		LocalDateTime refundedAt) {

	public static CommerceOrderResolutionRequestResponse from(CommerceOrderResolutionRequest request) {
		return new CommerceOrderResolutionRequestResponse(
				request.getId(),
				request.getUid(),
				request.getOrder().getUid(),
				request.getOrder().getOrderNumber(),
				request.getCustomer().getEmail(),
				request.getType().name(),
				request.getStatus().name(),
				request.getReason(),
				request.getDescription(),
				request.getPreviousOrderStatus().name(),
				request.getRequestedOrderStatus().name(),
				request.getDecisionNote(),
				request.getDecidedByUserId(),
				request.getDecidedByEmail(),
				request.getRefundStatus().name(),
				request.getRefundProvider(),
				request.getRefundReference(),
				request.getRefundFailureCode(),
				request.getRefundFailureMessageKey(),
				request.isStockRestored(),
				request.getOrder().getTotal(),
				request.getOrder().getCurrencyIso(),
				request.getCreatedAt(),
				request.getDecidedAt(),
				request.getRefundAttemptedAt(),
				request.getRefundedAt());
	}
}
