package com.backend.application.commerce.dto;

public record LoginCommerceCustomerCommand(
		String email,
		String password,
		Boolean rememberMe,
		String deviceFingerprint,
		String ipAddress,
		String userAgent) {
}
