package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

public record PaymentAttemptResponse(
		String attemptUid,
		String checkoutUid,
		String status,
		String provider,
		String currencyIso,
		PaymentAttemptTotalsResponse totals,
		LocalDateTime expiresAt,
		String failureMessageKey) {
}
