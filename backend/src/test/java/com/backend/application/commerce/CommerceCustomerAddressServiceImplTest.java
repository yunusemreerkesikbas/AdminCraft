package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.backend.application.commerce.dto.CommerceCustomerAddressCommand;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerAddress;
import com.backend.domain.commerce.repository.CommerceCustomerAddressRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.testutil.BaseServiceTest;

class CommerceCustomerAddressServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCustomerRepository customerRepository;
	@Mock private CommerceCustomerAddressRepository addressRepository;

	@InjectMocks
	private CommerceCustomerAddressServiceImpl service;

	@Test
	void create_ShouldClearExistingDefaults_WhenRequested() {
		CommerceCustomer customer = customer();
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
		when(addressRepository.save(any(CommerceCustomerAddress.class))).thenAnswer(invocation -> {
			CommerceCustomerAddress address = invocation.getArgument(0);
			address.setUid("address-uid");
			return address;
		});

		var response = service.create(principal(), addressCommand(true, true, "INDIVIDUAL"));

		assertThat(response.defaultDelivery()).isTrue();
		assertThat(response.defaultBilling()).isTrue();
		verify(addressRepository).clearDefaultDelivery(10L);
		verify(addressRepository).clearDefaultBilling(10L);
	}

	@Test
	void create_ShouldRejectCorporateAddressWithoutTaxFields() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer()));

		assertThatThrownBy(() -> service.create(principal(), addressCommand(false, false, "CORPORATE")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("commerce.customer.address.corporate.required");
	}

	@Test
	void update_ShouldOnlyLoadAddressOwnedByCurrentCustomer() {
		CommerceCustomerAddress address = new CommerceCustomerAddress();
		address.setCustomer(customer());
		address.setUid("address-uid");
		when(addressRepository.findByCustomerIdAndUid(10L, "address-uid")).thenReturn(Optional.of(address));
		when(addressRepository.save(any(CommerceCustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.update(principal(), "address-uid", addressCommand(false, false, "INDIVIDUAL"));

		verify(addressRepository).findByCustomerIdAndUid(10L, "address-uid");
	}

	@Test
	void update_ShouldPreserveDefaultFlags_WhenRequestOmitsThem() {
		CommerceCustomerAddress address = new CommerceCustomerAddress();
		address.setCustomer(customer());
		address.setUid("address-uid");
		address.setDefaultDelivery(true);
		address.setDefaultBilling(true);
		when(addressRepository.findByCustomerIdAndUid(10L, "address-uid")).thenReturn(Optional.of(address));
		when(addressRepository.save(any(CommerceCustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.update(principal(), "address-uid", addressCommand(null, null, "INDIVIDUAL"));

		assertThat(response.defaultDelivery()).isTrue();
		assertThat(response.defaultBilling()).isTrue();
	}

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "user@example.com", 1L);
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		customer.setEmailNormalized("user@example.com");
		customer.setFirstName("Emre");
		customer.setLastName("Erkesikbas");
		customer.setPhone("+905551112233");
		return customer;
	}

	private CommerceCustomerAddressCommand addressCommand(boolean delivery, boolean billing, String invoiceType) {
		return addressCommand(Boolean.valueOf(delivery), Boolean.valueOf(billing), invoiceType);
	}

	private CommerceCustomerAddressCommand addressCommand(Boolean delivery, Boolean billing, String invoiceType) {
		return new CommerceCustomerAddressCommand(
				"Home",
				"Emre",
				"Erkesikbas",
				"+905551112233",
				"TR",
				"Istanbul",
				"Kadikoy",
				"Address line",
				null,
				null,
				delivery,
				billing,
				invoiceType,
				null,
				null,
				null,
				null);
	}
}
