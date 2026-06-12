package com.backend.presentation.commerce;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCommerceCustomerRequest(
		@NotBlank(message = "{commerce.customer.email.required}") @Email(message = "{commerce.customer.email.invalid}") String email,
		@NotBlank(message = "{commerce.customer.password.required}") @Size(min = 8, max = 100, message = "{commerce.customer.password.invalid}") String password,
		@NotBlank(message = "{commerce.customer.firstName.required}") @Size(max = 100) String firstName,
		@NotBlank(message = "{commerce.customer.lastName.required}") @Size(max = 100) String lastName,
		@NotBlank(message = "{commerce.customer.phone.required}") @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "{commerce.customer.phone.invalid}") String phone,
		String gender,
		LocalDate birthDate,
		@AssertTrue(message = "{commerce.customer.terms.required}") boolean termsAccepted,
		@AssertTrue(message = "{commerce.customer.privacy.required}") boolean privacyAccepted,
		Boolean marketingEmailAccepted,
		Boolean marketingSmsAccepted,
		Boolean marketingPhoneAccepted,
		Boolean rememberMe,
		String deviceFingerprint,
		String source) {
}
