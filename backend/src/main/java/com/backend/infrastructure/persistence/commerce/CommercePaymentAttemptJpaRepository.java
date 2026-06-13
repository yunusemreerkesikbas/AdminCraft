package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;

interface CommercePaymentAttemptJpaRepository extends JpaRepository<CommercePaymentAttempt, Long> {

	@EntityGraph(attributePaths = { "checkout", "checkout.items", "checkout.cart", "checkout.cart.items" })
	Optional<CommercePaymentAttempt> findByCustomerIdAndUid(Long customerId, String uid);

	@Modifying(clearAutomatically = true)
	@Query("""
			UPDATE CommercePaymentAttempt attempt
			SET attempt.status = com.backend.domain.commerce.CommercePaymentAttemptStatus.EXPIRED
			WHERE attempt.customer.id = :customerId
				AND attempt.checkout.id = :checkoutId
				AND attempt.status = :pendingStatus
			""")
	int expirePendingAttemptsForCheckout(
			@Param("customerId") Long customerId,
			@Param("checkoutId") Long checkoutId,
			@Param("pendingStatus") CommercePaymentAttemptStatus pendingStatus);
}
