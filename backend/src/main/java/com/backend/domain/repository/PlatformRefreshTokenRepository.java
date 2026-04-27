package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.PlatformRefreshToken;

public interface PlatformRefreshTokenRepository {

    PlatformRefreshToken save(PlatformRefreshToken token);

    Optional<PlatformRefreshToken> findByTokenHash(String tokenHash);

    int revokeByTokenHash(String tokenHash);

    void revokeAllByUserId(Long userId);

    int deleteExpiredTokens();
}
