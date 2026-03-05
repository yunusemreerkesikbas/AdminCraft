package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.enums.TokenType;
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.domain.entity.PlatformVerificationToken;

import lombok.RequiredArgsConstructor;

/**
 * Implementation adapter for PlatformVerificationTokenRepository.
 * Delegates to JPA repository while implementing domain interface.
 */
@Component
@RequiredArgsConstructor
public class PlatformVerificationTokenRepositoryImpl implements PlatformVerificationTokenRepository {

    private final JpaPlatformVerificationTokenRepository jpaRepository;

    @Override
    public Optional<PlatformVerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public int revokeAllActiveTokensForAdmin(Long adminUserId, TokenType tokenType) {
        return jpaRepository.revokeAllActiveTokensForAdmin(adminUserId, tokenType);
    }

    @Override
    public PlatformVerificationToken save(PlatformVerificationToken token) {
        return toDomain(jpaRepository.save(toEntity(token)));
    }

    @Override
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private PlatformVerificationToken toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken source) {
        if (source == null) {
            return null;
        }

        PlatformVerificationToken target = PlatformVerificationToken.builder()
                .id(source.getId())
                .adminUser(toDomain(source.getAdminUser()))
                .tokenHash(source.getTokenHash())
                .tokenType(source.getTokenType())
                .status(source.getStatus())
                .targetValue(source.getTargetValue())
                .expiresAt(source.getExpiresAt())
                .attemptCount(source.getAttemptCount())
                .maxAttempts(source.getMaxAttempts())
                .usedAt(source.getUsedAt())
                .ipAddress(source.getIpAddress())
                .userAgent(source.getUserAgent())
                .createdAt(source.getCreatedAt())
                .build();
        return target;
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken toEntity(
            PlatformVerificationToken source) {
        if (source == null) {
            return null;
        }

        return com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken.builder()
                .id(source.getId())
                .adminUser(toEntity(source.getAdminUser()))
                .tokenHash(source.getTokenHash())
                .tokenType(source.getTokenType())
                .status(source.getStatus())
                .targetValue(source.getTargetValue())
                .expiresAt(source.getExpiresAt())
                .attemptCount(source.getAttemptCount())
                .maxAttempts(source.getMaxAttempts())
                .usedAt(source.getUsedAt())
                .ipAddress(source.getIpAddress())
                .userAgent(source.getUserAgent())
                .createdAt(source.getCreatedAt())
                .build();
    }

    private PlatformAdminUser toDomain(com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser source) {
        if (source == null) {
            return null;
        }
        return PlatformAdminUser.builder()
                .id(source.getId())
                .email(source.getEmail())
                .passwordHash(source.getPasswordHash())
                .fullName(source.getFullName())
                .isActive(source.getIsActive())
                .failedLoginAttempts(source.getFailedLoginAttempts())
                .lockedUntil(source.getLockedUntil())
                .lastLoginAt(source.getLastLoginAt())
                .lastLoginIp(source.getLastLoginIp())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser toEntity(PlatformAdminUser source) {
        if (source == null) {
            return null;
        }
        return com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser.builder()
                .id(source.getId())
                .email(source.getEmail())
                .passwordHash(source.getPasswordHash())
                .fullName(source.getFullName())
                .isActive(source.getIsActive())
                .failedLoginAttempts(source.getFailedLoginAttempts())
                .lockedUntil(source.getLockedUntil())
                .lastLoginAt(source.getLastLoginAt())
                .lastLoginIp(source.getLastLoginIp())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }
}
