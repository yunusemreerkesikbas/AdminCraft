package com.backend.presentation.commerce;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CommerceCustomerAddressRequest(
		@Size(max = 100) String label,
		@NotBlank(message = "{commerce.customer.firstName.required}") @Size(max = 100, message = "{commerce.customer.firstName.length}") String firstName,
		@NotBlank(message = "{commerce.customer.lastName.required}") @Size(max = 100, message = "{commerce.customer.lastName.length}") String lastName,
		@NotBlank(message = "{commerce.customer.phone.required}") @Size(min = 7, max = 30, message = "{commerce.customer.phone.invalid}") @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "{commerce.customer.phone.invalid}") String phone,
		@Size(min = 2, max = 2, message = "{commerce.customer.address.country.invalid}") String countryIso,
		@NotBlank(message = "{commerce.customer.address.city.required}") @Size(max = 100) String city,
		@NotBlank(message = "{commerce.customer.address.district.required}") @Size(max = 100) String district,
		@NotBlank(message = "{commerce.customer.address.line1.required}") @Size(max = 255) String addressLine1,
		@Size(max = 255) String addressLine2,
		@Size(max = 20) String postalCode,
		Boolean defaultDelivery,
		Boolean defaultBilling,
		@Pattern(regexp = "(?i)^(INDIVIDUAL|CORPORATE)$", message = "{commerce.customer.address.invoiceType.invalid}") String invoiceType,
		@Size(max = 200) String companyName,
		@Size(max = 50) String taxNumber,
		@Size(max = 100) String taxOffice,
		@Size(max = 50) String invoiceIdentityNumber) {
	public CommerceCustomerAddressRequest {
		label = trimToNull(label);
		firstName = trimToNull(firstName);
		lastName = trimToNull(lastName);
		phone = trimToNull(phone);
		countryIso = trimToNull(countryIso);
		city = trimToNull(city);
		district = trimToNull(district);
		addressLine1 = trimToNull(addressLine1);
		addressLine2 = trimToNull(addressLine2);
		postalCode = trimToNull(postalCode);
		invoiceType = trimToNull(invoiceType);
		companyName = trimToNull(companyName);
		taxNumber = trimToNull(taxNumber);
		taxOffice = trimToNull(taxOffice);
		invoiceIdentityNumber = trimToNull(invoiceIdentityNumber);
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
