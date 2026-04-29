package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.infrastructure.persistence.repository.entity.RefreshTokenEntity;

@Repository
interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :now WHERE t.tokenHash = :tokenHash AND t.revokedAt IS NULL AND t.expiresAt > :now")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :now WHERE t.userId = :userId AND t.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
