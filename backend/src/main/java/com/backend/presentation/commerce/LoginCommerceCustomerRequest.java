package com.backend.presentation.commerce;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginCommerceCustomerRequest(
		@NotBlank(message = "{commerce.customer.email.required}") @Email(message = "{commerce.customer.email.invalid}") String email,
		@NotBlank(message = "{commerce.customer.password.required}") String password,
		Boolean rememberMe,
		@Size(max = 255, message = "{commerce.customer.deviceFingerprint.invalid}") @Pattern(regexp = ValidationConstants.DEVICE_FINGERPRINT_PATTERN, message = "{commerce.customer.deviceFingerprint.invalid}") String deviceFingerprint) {
	public LoginCommerceCustomerRequest {
		email = trimToNull(email);
		deviceFingerprint = trimToNull(deviceFingerprint);
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
