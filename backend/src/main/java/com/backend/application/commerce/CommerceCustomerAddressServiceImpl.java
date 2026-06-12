package com.backend.application.commerce;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CommerceCustomerAddressCommand;
import com.backend.application.commerce.dto.CommerceCustomerAddressResponse;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerAddress;
import com.backend.domain.commerce.CommerceCustomerInvoiceType;
import com.backend.domain.commerce.repository.CommerceCustomerAddressRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceCustomerAddressServiceImpl implements CommerceCustomerAddressService {

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCustomerRepository customerRepository;
	private final CommerceCustomerAddressRepository addressRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CommerceCustomerAddressResponse> list(CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return addressRepository.findByCustomerId(principal.customerId()).stream()
				.map(CommerceCustomerAddressResponse::from)
				.toList();
	}

	@Override
	@Transactional
	public CommerceCustomerAddressResponse create(CommerceCustomerPrincipal principal, CommerceCustomerAddressCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomer customer = loadCustomer(principal);
		CommerceCustomerAddress address = new CommerceCustomerAddress();
		address.setCustomer(customer);
		apply(address, command, false);
		clearDefaultsIfRequested(customer.getId(), address);
		return CommerceCustomerAddressResponse.from(addressRepository.save(address));
	}

	@Override
	@Transactional
	public CommerceCustomerAddressResponse update(CommerceCustomerPrincipal principal, String addressUid, CommerceCustomerAddressCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomerAddress address = loadAddress(principal, addressUid);
		apply(address, command, true);
		clearDefaultsIfRequested(principal.customerId(), address);
		return CommerceCustomerAddressResponse.from(addressRepository.save(address));
	}

	@Override
	@Transactional
	public void delete(CommerceCustomerPrincipal principal, String addressUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		addressRepository.delete(loadAddress(principal, addressUid));
	}

	@Override
	@Transactional
	public CommerceCustomerAddressResponse setDefaultDelivery(CommerceCustomerPrincipal principal, String addressUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomerAddress address = loadAddress(principal, addressUid);
		addressRepository.clearDefaultDelivery(principal.customerId());
		address.setDefaultDelivery(true);
		return CommerceCustomerAddressResponse.from(addressRepository.save(address));
	}

	@Override
	@Transactional
	public CommerceCustomerAddressResponse setDefaultBilling(CommerceCustomerPrincipal principal, String addressUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomerAddress address = loadAddress(principal, addressUid);
		addressRepository.clearDefaultBilling(principal.customerId());
		address.setDefaultBilling(true);
		return CommerceCustomerAddressResponse.from(addressRepository.save(address));
	}

	private CommerceCustomer loadCustomer(CommerceCustomerPrincipal principal) {
		return customerRepository.findById(principal.customerId())
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.not.found"));
	}

	private CommerceCustomerAddress loadAddress(CommerceCustomerPrincipal principal, String addressUid) {
		return addressRepository.findByCustomerIdAndUid(principal.customerId(), addressUid)
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.address.not.found"));
	}

	private void apply(CommerceCustomerAddress address, CommerceCustomerAddressCommand command, boolean preserveDefaultFlags) {
		CommerceCustomerInvoiceType invoiceType = parseInvoiceType(command.invoiceType());
		if (invoiceType == CommerceCustomerInvoiceType.CORPORATE
				&& (isBlank(command.companyName()) || isBlank(command.taxNumber()) || isBlank(command.taxOffice()))) {
			throw new IllegalArgumentException("commerce.customer.address.corporate.required");
		}
		address.setLabel(clean(command.label()));
		address.setFirstName(command.firstName().trim());
		address.setLastName(command.lastName().trim());
		address.setPhone(command.phone().trim());
		address.setCountryIso(isBlank(command.countryIso()) ? "TR" : command.countryIso().trim().toUpperCase(Locale.ROOT));
		address.setCity(command.city().trim());
		address.setDistrict(command.district().trim());
		address.setAddressLine1(command.addressLine1().trim());
		address.setAddressLine2(clean(command.addressLine2()));
		address.setPostalCode(clean(command.postalCode()));
		address.setDefaultDelivery(resolveDefaultFlag(command.defaultDelivery(), address.isDefaultDelivery(), preserveDefaultFlags));
		address.setDefaultBilling(resolveDefaultFlag(command.defaultBilling(), address.isDefaultBilling(), preserveDefaultFlags));
		address.setInvoiceType(invoiceType);
		address.setCompanyName(clean(command.companyName()));
		address.setTaxNumber(clean(command.taxNumber()));
		address.setTaxOffice(clean(command.taxOffice()));
		address.setInvoiceIdentityNumber(clean(command.invoiceIdentityNumber()));
	}

	private void clearDefaultsIfRequested(Long customerId, CommerceCustomerAddress address) {
		if (address.isDefaultDelivery()) {
			addressRepository.clearDefaultDelivery(customerId);
		}
		if (address.isDefaultBilling()) {
			addressRepository.clearDefaultBilling(customerId);
		}
	}

	private CommerceCustomerInvoiceType parseInvoiceType(String invoiceType) {
		if (invoiceType == null || invoiceType.isBlank()) {
			return CommerceCustomerInvoiceType.INDIVIDUAL;
		}
		return CommerceCustomerInvoiceType.valueOf(invoiceType.trim().toUpperCase(Locale.ROOT));
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String clean(String value) {
		return isBlank(value) ? null : value.trim();
	}

	private boolean resolveDefaultFlag(Boolean requested, boolean current, boolean preserveWhenMissing) {
		if (requested != null) {
			return requested;
		}
		return preserveWhenMissing && current;
	}
}
