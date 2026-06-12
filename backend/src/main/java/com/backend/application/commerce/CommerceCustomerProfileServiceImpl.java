package com.backend.application.commerce;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CommerceCustomerResponse;
import com.backend.application.commerce.dto.UpdateCommerceCustomerProfileCommand;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerGender;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceCustomerProfileServiceImpl implements CommerceCustomerProfileService {

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCustomerRepository customerRepository;

	@Override
	@Transactional(readOnly = true)
	public CommerceCustomerResponse getMe(CommerceCustomerPrincipal principal) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return CommerceCustomerResponse.from(loadCustomer(principal));
	}

	@Override
	@Transactional
	public CommerceCustomerResponse updateMe(CommerceCustomerPrincipal principal, UpdateCommerceCustomerProfileCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceCustomer customer = loadCustomer(principal);
		customer.setFirstName(command.firstName().trim());
		customer.setLastName(command.lastName().trim());
		customer.setPhone(command.phone().trim());
		customer.setGender(parseGender(command.gender()));
		customer.setBirthDate(command.birthDate());
		return CommerceCustomerResponse.from(customerRepository.save(customer));
	}

	private CommerceCustomer loadCustomer(CommerceCustomerPrincipal principal) {
		return customerRepository.findById(principal.customerId())
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.not.found"));
	}

	private CommerceCustomerGender parseGender(String gender) {
		if (gender == null || gender.isBlank()) {
			return null;
		}
		return CommerceCustomerGender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
	}
}
