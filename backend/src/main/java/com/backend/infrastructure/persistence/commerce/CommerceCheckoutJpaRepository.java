package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutStatus;

import jakarta.persistence.LockModeType;

interface CommerceCheckoutJpaRepository extends JpaRepository<CommerceCheckout, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "customer", "cart", "items" })
	@Query("SELECT checkout FROM CommerceCheckout checkout WHERE checkout.id = :id")
	Optional<CommerceCheckout> findByIdForUpdate(@Param("id") Long id);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceCheckout> findByCustomerIdAndUid(Long customerId, String uid);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceCheckout> findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(
			Long customerId,
			Collection<CommerceCheckoutStatus> statuses,
			LocalDateTime now);

	@Modifying(clearAutomatically = true)
	@Query("""
			UPDATE CommerceCheckout checkout
			SET checkout.status = com.backend.domain.commerce.CommerceCheckoutStatus.EXPIRED
			WHERE checkout.customer.id = :customerId
				AND checkout.status IN :statuses
				AND checkout.expiresAt > :now
			""")
	int expireOpenCheckouts(
			@Param("customerId") Long customerId,
			@Param("statuses") Collection<CommerceCheckoutStatus> statuses,
			@Param("now") LocalDateTime now);
}
