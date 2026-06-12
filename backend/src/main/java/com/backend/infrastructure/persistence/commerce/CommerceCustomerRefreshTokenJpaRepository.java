package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceCustomerRefreshToken;

interface CommerceCustomerRefreshTokenJpaRepository extends JpaRepository<CommerceCustomerRefreshToken, Long> {

	Optional<CommerceCustomerRefreshToken> findByTokenHash(String tokenHash);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CommerceCustomerRefreshToken t SET t.revokedAt = :now WHERE t.tokenHash = :tokenHash AND t.revokedAt IS NULL")
	int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);
}
