package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class CommerceCustomerRequestValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void addressRequest_ShouldAllowBlankCountryIsoForDefaultAndRejectInvalidInvoiceType() {
		CommerceCustomerAddressRequest request = new CommerceCustomerAddressRequest(
				null,
				"Emre",
				"Erkesikbas",
				"+90 555 111 2233",
				" ",
				"Istanbul",
				"Kadikoy",
				"Address line",
				null,
				null,
				false,
				false,
				"INVALID",
				null,
				null,
				null,
				null);

		assertThat(request.countryIso()).isNull();
		assertThat(validator.validate(request))
				.anySatisfy(violation -> assertThat(violation.getMessageTemplate())
						.isEqualTo("{commerce.customer.address.invoiceType.invalid}"));
	}

	@Test
	void registerRequest_ShouldRejectInvalidGenderFutureBirthDateAndDeviceFingerprint() {
		RegisterCommerceCustomerRequest request = new RegisterCommerceCustomerRequest(
				"user@example.com",
				"Password123",
				"Emre",
				"Erkesikbas",
				"+905551112233",
				"invalid",
				LocalDate.now().plusDays(1),
				true,
				true,
				false,
				false,
				false,
				false,
				"invalid fingerprint!",
				"storefront");

		assertThat(validator.validate(request))
				.extracting(violation -> violation.getMessageTemplate())
				.contains(
						"{commerce.customer.gender.invalid}",
						"{commerce.customer.birthDate.past}",
						"{commerce.customer.deviceFingerprint.invalid}");
	}

	@Test
	void updateProfileRequest_ShouldTrimFieldsAndValidatePhoneSize() {
		UpdateCommerceCustomerProfileRequest request = new UpdateCommerceCustomerProfileRequest(
				" Emre ",
				" Erkesikbas ",
				"123",
				" male ",
				null);

		assertThat(request.firstName()).isEqualTo("Emre");
		assertThat(request.lastName()).isEqualTo("Erkesikbas");
		assertThat(request.gender()).isEqualTo("male");
		assertThat(validator.validate(request))
				.anySatisfy(violation -> assertThat(violation.getMessageTemplate())
						.isEqualTo("{commerce.customer.phone.invalid}"));
	}

	@Test
	void loginRequest_ShouldRejectOversizedDeviceFingerprint() {
		LoginCommerceCustomerRequest request = new LoginCommerceCustomerRequest(
				"user@example.com",
				"Password123",
				false,
				"a".repeat(256));

		assertThat(validator.validate(request))
				.anySatisfy(violation -> assertThat(violation.getMessageTemplate())
						.isEqualTo("{commerce.customer.deviceFingerprint.invalid}"));
	}
}
