package com.backend.application.commerce.dto;

import java.time.LocalDate;

import com.backend.domain.commerce.CommerceCustomer;

public record CommerceCustomerResponse(
		String uid,
		String email,
		String firstName,
		String lastName,
		String phone,
		String gender,
		LocalDate birthDate,
		String status,
		boolean emailVerified) {

	public static CommerceCustomerResponse from(CommerceCustomer customer) {
		return new CommerceCustomerResponse(
				customer.getUid(),
				customer.getEmail(),
				customer.getFirstName(),
				customer.getLastName(),
				customer.getPhone(),
				customer.getGender() == null ? null : customer.getGender().name(),
				customer.getBirthDate(),
				customer.getStatus().name(),
				customer.isEmailVerified());
	}
}
