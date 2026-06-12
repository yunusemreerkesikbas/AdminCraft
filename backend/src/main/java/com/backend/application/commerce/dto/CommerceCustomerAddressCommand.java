package com.backend.application.commerce.dto;

public record CommerceCustomerAddressCommand(
		String label,
		String firstName,
		String lastName,
		String phone,
		String countryIso,
		String city,
		String district,
		String addressLine1,
		String addressLine2,
		String postalCode,
		Boolean defaultDelivery,
		Boolean defaultBilling,
		String invoiceType,
		String companyName,
		String taxNumber,
		String taxOffice,
		String invoiceIdentityNumber) {
}
