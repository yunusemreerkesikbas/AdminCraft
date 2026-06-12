package com.backend.application.commerce.dto;

import java.time.LocalDate;

public record RegisterCommerceCustomerCommand(
		String email,
		String password,
		String firstName,
		String lastName,
		String phone,
		String gender,
		LocalDate birthDate,
		boolean termsAccepted,
		boolean privacyAccepted,
		Boolean marketingEmailAccepted,
		Boolean marketingSmsAccepted,
		Boolean marketingPhoneAccepted,
		Boolean rememberMe,
		String deviceFingerprint,
		String source,
		String cartToken,
		String ipAddress,
		String userAgent) {
}
