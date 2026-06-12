package com.backend.application.commerce.dto;

import com.backend.domain.commerce.CommerceCustomerAddress;

public record CommerceCustomerAddressResponse(
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
		boolean defaultDelivery,
		boolean defaultBilling,
		String invoiceType,
		String companyName,
		String taxNumber,
		String taxOffice,
		String invoiceIdentityNumber) {

	public static CommerceCustomerAddressResponse from(CommerceCustomerAddress address) {
		return new CommerceCustomerAddressResponse(
				address.getUid(),
				address.getLabel(),
				address.getFirstName(),
				address.getLastName(),
				address.getPhone(),
				address.getCountryIso(),
				address.getCity(),
				address.getDistrict(),
				address.getAddressLine1(),
				address.getAddressLine2(),
				address.getPostalCode(),
				address.isDefaultDelivery(),
				address.isDefaultBilling(),
				address.getInvoiceType().name(),
				address.getCompanyName(),
				address.getTaxNumber(),
				address.getTaxOffice(),
				address.getInvoiceIdentityNumber());
	}
}
