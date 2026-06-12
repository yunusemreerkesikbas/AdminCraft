package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceCustomerAddress;

interface CommerceCustomerAddressJpaRepository extends JpaRepository<CommerceCustomerAddress, Long> {

	List<CommerceCustomerAddress> findByCustomerIdOrderByIdAsc(Long customerId);

	Optional<CommerceCustomerAddress> findByCustomerIdAndUid(Long customerId, String uid);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CommerceCustomerAddress a SET a.defaultDelivery = false WHERE a.customer.id = :customerId AND a.defaultDelivery = true")
	int clearDefaultDelivery(@Param("customerId") Long customerId);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CommerceCustomerAddress a SET a.defaultBilling = false WHERE a.customer.id = :customerId AND a.defaultBilling = true")
	int clearDefaultBilling(@Param("customerId") Long customerId);
}
