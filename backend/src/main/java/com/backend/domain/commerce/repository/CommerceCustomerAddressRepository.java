package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.commerce.CommerceCustomerAddress;

public interface CommerceCustomerAddressRepository {

	CommerceCustomerAddress save(CommerceCustomerAddress address);

	List<CommerceCustomerAddress> findByCustomerId(Long customerId);

	Optional<CommerceCustomerAddress> findByCustomerIdAndUid(Long customerId, String uid);

	Optional<CommerceCustomerAddress> findFirstByCustomerIdAndDefaultDeliveryTrueOrderByIdAsc(Long customerId);

	Optional<CommerceCustomerAddress> findFirstByCustomerIdAndDefaultBillingTrueOrderByIdAsc(Long customerId);

	void delete(CommerceCustomerAddress address);

	int clearDefaultDelivery(Long customerId);

	int clearDefaultBilling(Long customerId);
}
