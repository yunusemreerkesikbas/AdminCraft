package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceCustomer;

interface CommerceCustomerJpaRepository extends JpaRepository<CommerceCustomer, Long> {

	Optional<CommerceCustomer> findByUid(String uid);

	Optional<CommerceCustomer> findByEmailNormalized(String emailNormalized);

	boolean existsByEmailNormalized(String emailNormalized);
}
