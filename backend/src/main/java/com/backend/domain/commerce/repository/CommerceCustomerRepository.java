package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceCustomer;

public interface CommerceCustomerRepository {

	CommerceCustomer save(CommerceCustomer customer);

	Optional<CommerceCustomer> findById(Long id);

	Optional<CommerceCustomer> findByUid(String uid);

	Optional<CommerceCustomer> findByEmailNormalized(String emailNormalized);

	boolean existsByEmailNormalized(String emailNormalized);
}
