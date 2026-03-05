package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.repository.PlatformAdminUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformAdminUserPersistenceAdapter implements PlatformAdminUserRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository jpaRepository;

    @Override
    public Optional<PlatformAdminUser> findByEmailAndIsActiveTrue(String email) {
        return jpaRepository.findByEmailAndIsActiveTrue(email).map(this::toDomain);
    }

    @Override
    public PlatformAdminUser save(PlatformAdminUser admin) {
        return toDomain(jpaRepository.save(toEntity(admin)));
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
