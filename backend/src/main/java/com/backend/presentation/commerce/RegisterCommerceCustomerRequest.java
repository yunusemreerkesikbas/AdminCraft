package com.backend.presentation.commerce;

import java.time.LocalDate;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCommerceCustomerRequest(
		@NotBlank(message = "{commerce.customer.email.required}") @Email(message = "{commerce.customer.email.invalid}") String email,
		@NotBlank(message = "{commerce.customer.password.required}") @Size(min = 8, max = 100, message = "{commerce.customer.password.invalid}") String password,
		@NotBlank(message = "{commerce.customer.firstName.required}") @Size(max = 100, message = "{commerce.customer.firstName.length}") String firstName,
		@NotBlank(message = "{commerce.customer.lastName.required}") @Size(max = 100, message = "{commerce.customer.lastName.length}") String lastName,
		@NotBlank(message = "{commerce.customer.phone.required}") @Size(min = 7, max = 30, message = "{commerce.customer.phone.invalid}") @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "{commerce.customer.phone.invalid}") String phone,
		@Pattern(regexp = "(?i)^(FEMALE|MALE|OTHER|UNSPECIFIED)$", message = "{commerce.customer.gender.invalid}") String gender,
		@Past(message = "{commerce.customer.birthDate.past}") LocalDate birthDate,
		@AssertTrue(message = "{commerce.customer.terms.required}") boolean termsAccepted,
		@AssertTrue(message = "{commerce.customer.privacy.required}") boolean privacyAccepted,
		Boolean marketingEmailAccepted,
		Boolean marketingSmsAccepted,
		Boolean marketingPhoneAccepted,
		Boolean rememberMe,
		@Size(max = 255, message = "{commerce.customer.deviceFingerprint.invalid}") @Pattern(regexp = ValidationConstants.DEVICE_FINGERPRINT_PATTERN, message = "{commerce.customer.deviceFingerprint.invalid}") String deviceFingerprint,
		String source) {
	public RegisterCommerceCustomerRequest {
		email = trimToNull(email);
		firstName = trimToNull(firstName);
		lastName = trimToNull(lastName);
		phone = trimToNull(phone);
		gender = trimToNull(gender);
		deviceFingerprint = trimToNull(deviceFingerprint);
		source = trimToNull(source);
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
