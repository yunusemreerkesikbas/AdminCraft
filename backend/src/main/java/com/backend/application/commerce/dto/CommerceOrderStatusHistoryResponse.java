package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceOrderStatusHistory;

public record CommerceOrderStatusHistoryResponse(
		String uid,
		String fromStatus,
		String toStatus,
		String carrierName,
		String trackingNumber,
		String trackingUrl,
		String internalNote,
		Long changedByUserId,
		String changedByEmail,
		LocalDateTime createdAt) {

	public static CommerceOrderStatusHistoryResponse from(CommerceOrderStatusHistory history) {
		return new CommerceOrderStatusHistoryResponse(
				history.getUid(),
				history.getFromStatus().name(),
				history.getToStatus().name(),
				history.getShippingCarrierName(),
				history.getShippingTrackingNumber(),
				history.getShippingTrackingUrl(),
				history.getInternalNote(),
				history.getChangedByUserId(),
				history.getChangedByEmail(),
				history.getCreatedAt());
	}
}
