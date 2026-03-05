package com.backend.infrastructure.persistence.platform.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformVerificationToken;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformVerificationTokenMapper {

    private final PlatformAdminUserMapper platformAdminUserMapper;

    public PlatformVerificationToken toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken source) {
        if (source == null) {
            return null;
        }
        return PlatformVerificationToken.builder()
                .id(source.getId())
                .adminUser(platformAdminUserMapper.toDomain(source.getAdminUser()))
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

    public com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken toEntity(
            PlatformVerificationToken source) {
        if (source == null) {
            return null;
        }
        return com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken.builder()
                .id(source.getId())
                .adminUser(platformAdminUserMapper.toEntity(source.getAdminUser()))
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
}
