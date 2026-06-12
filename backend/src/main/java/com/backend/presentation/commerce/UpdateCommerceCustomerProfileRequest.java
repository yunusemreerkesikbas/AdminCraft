package com.backend.presentation.commerce;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCommerceCustomerProfileRequest(
		@NotBlank(message = "{commerce.customer.firstName.required}") @Size(max = 100, message = "{commerce.customer.firstName.length}") String firstName,
		@NotBlank(message = "{commerce.customer.lastName.required}") @Size(max = 100, message = "{commerce.customer.lastName.length}") String lastName,
		@NotBlank(message = "{commerce.customer.phone.required}") @Size(min = 7, max = 30, message = "{commerce.customer.phone.invalid}") @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "{commerce.customer.phone.invalid}") String phone,
		@Pattern(regexp = "(?i)^(FEMALE|MALE|OTHER|UNSPECIFIED)$", message = "{commerce.customer.gender.invalid}") String gender,
		@Past(message = "{commerce.customer.birthDate.past}") LocalDate birthDate) {
	public UpdateCommerceCustomerProfileRequest {
		firstName = trimToNull(firstName);
		lastName = trimToNull(lastName);
		phone = trimToNull(phone);
		gender = trimToNull(gender);
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
