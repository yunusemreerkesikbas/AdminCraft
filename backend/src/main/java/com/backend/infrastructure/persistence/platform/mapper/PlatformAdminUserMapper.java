package com.backend.infrastructure.persistence.platform.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformAdminUser;

@Component
public class PlatformAdminUserMapper {

    public PlatformAdminUser toDomain(com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser source) {
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

    public com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser toEntity(PlatformAdminUser source) {
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
