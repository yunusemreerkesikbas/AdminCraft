package com.backend.application.commerce.dto;

public record CommerceCustomerAuthResponse(
		String accessToken,
		long expiresInSeconds,
		CommerceCustomerResponse customer) {
}
