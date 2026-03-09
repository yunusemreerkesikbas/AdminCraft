package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformVerificationToken;
import com.backend.domain.enums.TokenType;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformVerificationTokenMapper;

import lombok.RequiredArgsConstructor;

/**
 * Implementation adapter for PlatformVerificationTokenRepository.
 * Delegates to JPA repository while implementing domain interface.
 */
@Component
@RequiredArgsConstructor
public class PlatformVerificationTokenRepositoryImpl implements PlatformVerificationTokenRepository {

    private final JpaPlatformVerificationTokenRepository jpaRepository;
    private final PlatformVerificationTokenMapper platformVerificationTokenMapper;

    @Override
    public Optional<PlatformVerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(platformVerificationTokenMapper::toDomain);
    }

    @Override
    public int revokeAllActiveTokensForAdmin(Long adminUserId, TokenType tokenType) {
        return jpaRepository.revokeAllActiveTokensForAdmin(adminUserId, tokenType);
    }

    @Override
    public PlatformVerificationToken save(PlatformVerificationToken token) {
        com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken saved =
                jpaRepository.save(platformVerificationTokenMapper.toEntity(token));

        return platformVerificationTokenMapper.toDomain(saved);
    }

    @Override
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
