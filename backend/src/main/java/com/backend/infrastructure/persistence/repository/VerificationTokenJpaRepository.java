package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;

@Repository
public interface VerificationTokenJpaRepository extends JpaRepository<VerificationToken, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<VerificationToken> findByTokenHash(String tokenHash);

    Optional<VerificationToken> findByTokenHashAndStatus(String tokenHash, TokenStatus status);

    List<VerificationToken> findByUserIdAndTokenType(Long userId, TokenType tokenType);

    List<VerificationToken> findByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status);

    @Query("SELECT t FROM VerificationToken t WHERE t.user.id = :userId AND t.tokenType = :tokenType AND t.status = 'ACTIVE' AND t.expiresAt > :now")
    Optional<VerificationToken> findActiveTokenByUserAndType(
            @Param("userId") Long userId,
            @Param("tokenType") TokenType tokenType,
            @Param("now") LocalDateTime now);

    @Query("SELECT t FROM VerificationToken t WHERE t.status = 'ACTIVE' AND t.expiresAt < :now")
    List<VerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationToken t SET t.status = 'REVOKED' WHERE t.user.id = :userId AND t.tokenType = :tokenType AND t.status = 'ACTIVE'")
    void revokeAllActiveTokensForUser(@Param("userId") Long userId, @Param("tokenType") TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE (t.expiresAt < :before AND t.status != 'ACTIVE') OR (t.status = 'ACTIVE' AND t.expiresAt < :activeCutoff)")
    void deleteExpiredTokens(@Param("before") LocalDateTime before, @Param("activeCutoff") LocalDateTime activeCutoff);

    long countByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status);

    boolean existsByTokenHash(String tokenHash);
}
