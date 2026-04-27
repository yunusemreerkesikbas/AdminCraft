package com.backend.infrastructure.persistence.platform.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformRefreshToken;

@Component
public class PlatformRefreshTokenMapper {

    public PlatformRefreshToken toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformRefreshToken source) {
        if (source == null) return null;
        return PlatformRefreshToken.builder()
                .id(source.getId())
                .userId(source.getUserId())
                .tokenHash(source.getTokenHash())
                .expiresAt(source.getExpiresAt())
                .revokedAt(source.getRevokedAt())
                .createdAt(source.getCreatedAt())
                .build();
    }

    public com.backend.infrastructure.persistence.platform.entity.PlatformRefreshToken toEntity(
            PlatformRefreshToken source) {
        if (source == null) return null;
        return com.backend.infrastructure.persistence.platform.entity.PlatformRefreshToken.builder()
                .id(source.getId())
                .userId(source.getUserId())
                .tokenHash(source.getTokenHash())
                .expiresAt(source.getExpiresAt())
                .revokedAt(source.getRevokedAt())
                .createdAt(source.getCreatedAt())
                .build();
    }
}
