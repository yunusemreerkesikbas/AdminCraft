package com.backend.infrastructure.persistence.repository.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.RefreshToken;
import com.backend.infrastructure.persistence.repository.entity.RefreshTokenEntity;

@Component
public class RefreshTokenMapper {

    public RefreshToken toDomain(RefreshTokenEntity source) {
        if (source == null) return null;
        return RefreshToken.builder()
                .id(source.getId())
                .userId(source.getUserId())
                .tokenHash(source.getTokenHash())
                .expiresAt(source.getExpiresAt())
                .revokedAt(source.getRevokedAt())
                .createdAt(source.getCreatedAt())
                .build();
    }

    public RefreshTokenEntity toEntity(RefreshToken source) {
        if (source == null) return null;
        return RefreshTokenEntity.builder()
                .id(source.getId())
                .userId(source.getUserId())
                .tokenHash(source.getTokenHash())
                .expiresAt(source.getExpiresAt())
                .revokedAt(source.getRevokedAt())
                .createdAt(source.getCreatedAt())
                .build();
    }
}
