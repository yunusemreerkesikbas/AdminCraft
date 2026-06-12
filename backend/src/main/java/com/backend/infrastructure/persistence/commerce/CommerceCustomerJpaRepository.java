package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceCustomer;

import jakarta.persistence.LockModeType;

interface CommerceCustomerJpaRepository extends JpaRepository<CommerceCustomer, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select customer from CommerceCustomer customer where customer.id = :id")
	Optional<CommerceCustomer> findByIdForUpdate(@Param("id") Long id);

	Optional<CommerceCustomer> findByUid(String uid);

	Optional<CommerceCustomer> findByEmailNormalized(String emailNormalized);

	boolean existsByEmailNormalized(String emailNormalized);
}
