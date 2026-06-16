package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@EntityGraph(attributePaths = { "customer", "checkout", "checkout.items", "checkout.cart", "checkout.cart.items" })
	Optional<CommercePaymentAttempt> findFirstByProviderAndProviderReferenceOrderByIdDesc(String provider, String providerReference);

	@EntityGraph(attributePaths = { "customer", "checkout" })
	@Query("""
			select attempt from CommercePaymentAttempt attempt
			join attempt.customer customer
			where (:status is null or attempt.status = :status)
				and (:search is null
					or lower(attempt.uid) like concat('%', :search, '%')
					or lower(attempt.provider) like concat('%', :search, '%')
					or lower(coalesce(attempt.providerReference, '')) like concat('%', :search, '%')
					or lower(coalesce(attempt.providerTransactionId, '')) like concat('%', :search, '%')
					or lower(customer.email) like concat('%', :search, '%')
					or lower(customer.firstName) like concat('%', :search, '%')
					or lower(customer.lastName) like concat('%', :search, '%'))
			""")
	Page<CommercePaymentAttempt> findAdminPaymentAttempts(
			@Param("search") String search,
			@Param("status") CommercePaymentAttemptStatus status,
			Pageable pageable);

	@Modifying(clearAutomatically = true)
	@Query("""
			UPDATE CommercePaymentAttempt attempt
			SET attempt.status = :initializingStatus
			WHERE attempt.id = :attemptId
				AND attempt.status = :pendingStatus
				AND attempt.providerReference IS NULL
				AND attempt.expiresAt > :now
			""")
	int reservePendingAttemptInitialization(
			@Param("attemptId") Long attemptId,
			@Param("pendingStatus") CommercePaymentAttemptStatus pendingStatus,
			@Param("initializingStatus") CommercePaymentAttemptStatus initializingStatus,
			@Param("now") LocalDateTime now);

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

	long countByStatusAndCreatedAtGreaterThanEqual(
			CommercePaymentAttemptStatus status,
			LocalDateTime createdAt);
}
