package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findById(Long id);

    void deleteById(Long id);

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    Optional<VerificationToken> findByTokenHashAndStatus(String tokenHash, TokenStatus status);

    List<VerificationToken> findByUserIdAndTokenType(Long userId, TokenType tokenType);

    List<VerificationToken> findByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status);

    Optional<VerificationToken> findActiveTokenByUserAndType(Long userId, TokenType tokenType);

    List<VerificationToken> findExpiredTokens(LocalDateTime now);

    void revokeAllActiveTokensForUser(Long userId, TokenType tokenType);

    void deleteExpiredTokens(LocalDateTime before, LocalDateTime activeCutoff);

    long countByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status);

    boolean existsByTokenHash(String tokenHash);
}
