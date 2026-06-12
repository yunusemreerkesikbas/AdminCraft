package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;

import jakarta.persistence.LockModeType;

interface CommerceCartJpaRepository extends JpaRepository<CommerceCart, Long> {

    @EntityGraph(attributePaths = { "items" })
    Optional<CommerceCart> findByTokenHashAndStatus(String tokenHash, CommerceCartStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "items" })
	@Query("select cart from CommerceCart cart where cart.tokenHash = :tokenHash and cart.status = :status")
	Optional<CommerceCart> findByTokenHashAndStatusForUpdate(
			@Param("tokenHash") String tokenHash,
			@Param("status") CommerceCartStatus status);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceCart> findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
			Long customerId,
			CommerceCartStatus status,
			LocalDateTime now);
}
