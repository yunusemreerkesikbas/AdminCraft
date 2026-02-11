package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.enums.TokenType;
import com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken;

/**
 * JPA repository interface for PlatformVerificationToken entity.
 * Internal infrastructure component - use domain repository from Application layer.
 */
@Repository
interface JpaPlatformVerificationTokenRepository extends JpaRepository<PlatformVerificationToken, Long> {

    Optional<PlatformVerificationToken> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional("platformTransactionManager")
    @Query("""
            UPDATE PlatformVerificationToken t
            SET t.status = com.backend.domain.enums.TokenStatus.REVOKED
            WHERE t.adminUser.id = :adminUserId
              AND t.tokenType = :tokenType
              AND t.status = com.backend.domain.enums.TokenStatus.ACTIVE
            """)
    int revokeAllActiveTokensForAdmin(
            @Param("adminUserId") Long adminUserId,
            @Param("tokenType") TokenType tokenType);
    
    @Modifying
    @Transactional("platformTransactionManager")
    @Query("""
            DELETE FROM PlatformVerificationToken t
            WHERE t.expiresAt < :now
            """)
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
