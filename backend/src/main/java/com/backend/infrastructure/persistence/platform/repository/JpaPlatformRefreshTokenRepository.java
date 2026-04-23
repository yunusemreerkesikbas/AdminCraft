package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.infrastructure.persistence.platform.entity.PlatformRefreshToken;

@Repository
interface JpaPlatformRefreshTokenRepository extends JpaRepository<PlatformRefreshToken, Long> {

    Optional<PlatformRefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional("platformTransactionManager")
    @Query("UPDATE PlatformRefreshToken t SET t.revokedAt = :now WHERE t.tokenHash = :tokenHash AND t.revokedAt IS NULL AND t.expiresAt > :now")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional("platformTransactionManager")
    @Query("UPDATE PlatformRefreshToken t SET t.revokedAt = :now WHERE t.userId = :userId AND t.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional("platformTransactionManager")
    @Query("DELETE FROM PlatformRefreshToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
