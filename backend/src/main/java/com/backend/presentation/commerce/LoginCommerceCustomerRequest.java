package com.backend.presentation.commerce;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommerceCustomerRequest(
		@NotBlank(message = "{commerce.customer.email.required}") @Email(message = "{commerce.customer.email.invalid}") String email,
		@NotBlank(message = "{commerce.customer.password.required}") String password,
		Boolean rememberMe,
		String deviceFingerprint) {
}
