package com.backend.presentation.commerce;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCommerceCustomerProfileRequest(
		@NotBlank(message = "{commerce.customer.firstName.required}") @Size(max = 100) String firstName,
		@NotBlank(message = "{commerce.customer.lastName.required}") @Size(max = 100) String lastName,
		@NotBlank(message = "{commerce.customer.phone.required}") @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "{commerce.customer.phone.invalid}") String phone,
		String gender,
		LocalDate birthDate) {
}
