package com.backend.application.commerce.dto;

import java.time.LocalDate;

public record UpdateCommerceCustomerProfileCommand(
		String firstName,
		String lastName,
		String phone,
		String gender,
		LocalDate birthDate) {
}
