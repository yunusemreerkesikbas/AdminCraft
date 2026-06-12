package com.backend.application.commerce.dto;

public record CheckoutAddressSnapshotResponse(
		String uid,
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
		String invoiceType,
		String companyName,
		String taxNumber,
		String taxOffice,
		String invoiceIdentityNumber) {
}
