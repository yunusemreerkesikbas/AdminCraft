package com.backend.application.commerce.dto;

public record PaymentInitializeResponse(
		String attemptUid,
		String status,
		String provider,
		String paymentPageUrl) {
}
