package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.enums.TokenType;
import com.backend.domain.entity.PlatformVerificationToken;

/**
 * Domain repository interface for platform verification tokens.
 * Used by Application layer for OTP token management.
 */
public interface PlatformVerificationTokenRepository {

    /**
     * Find token by hash.
     * @param tokenHash SHA-256 hash of token
     * @return Optional containing token if found
     */
    Optional<PlatformVerificationToken> findByTokenHash(String tokenHash);

    /**
     * Revoke all active tokens for admin user.
     * @param adminUserId admin user ID
     * @param tokenType token type to revoke
     * @return number of tokens revoked
     */
    int revokeAllActiveTokensForAdmin(Long adminUserId, TokenType tokenType);
    
    /**
     * Save platform verification token.
     * @param token token to save
     * @return saved token
     */
    PlatformVerificationToken save(PlatformVerificationToken token);
    
    /**
     * Delete expired platform verification tokens.
     * @return number of tokens deleted
     */
    int deleteExpiredTokens();
}
