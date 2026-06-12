package com.backend.application.commerce.dto;

public record LoginCommerceCustomerCommand(
		String email,
		String password,
		Boolean rememberMe,
		String deviceFingerprint,
		String cartToken,
		String ipAddress,
		String userAgent) {
}
