package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommercePaymentAttempt;

public record CommerceAdminOrderPaymentResponse(
		String attemptUid,
		String status,
		String provider,
		String providerReference,
		String providerTransactionId,
		String failureCode,
		String failureMessageKey,
		LocalDateTime createdAt,
		LocalDateTime expiresAt) {

	public static CommerceAdminOrderPaymentResponse from(CommercePaymentAttempt attempt) {
		return new CommerceAdminOrderPaymentResponse(
				attempt.getUid(),
				attempt.getStatus().name(),
				attempt.getProvider(),
				attempt.getProviderReference(),
				attempt.getProviderTransactionId(),
				attempt.getFailureCode(),
				attempt.getFailureMessageKey(),
				attempt.getCreatedAt(),
				attempt.getExpiresAt());
	}
}
