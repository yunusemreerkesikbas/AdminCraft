package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.RefreshToken;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    int revokeByTokenHash(String tokenHash);

    void revokeAllByUserId(Long userId);

    int deleteExpiredTokens();
}
