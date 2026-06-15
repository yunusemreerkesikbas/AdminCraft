package com.backend.application.commerce.dto;

public record InitializePaymentAttemptCommand(
		String attemptUid,
		String callbackUrl,
		String clientIp) {
}
