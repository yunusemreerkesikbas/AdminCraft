package com.backend.infrastructure.persistence.platform.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.backend.domain.entity.PlatformAdminUser;

class PlatformAdminUserMapperTest {

    private final PlatformAdminUserMapper mapper = new PlatformAdminUserMapper();

    @Test
    void toDomainMapsAllFields() {
        com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser source =
                com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser.builder()
                        .id(10L)
                        .email("admin@example.com")
                        .passwordHash("hash")
                        .fullName("Admin User")
                        .isActive(true)
                        .failedLoginAttempts(2)
                        .lockedUntil(LocalDateTime.of(2026, 1, 1, 12, 0))
                        .lastLoginAt(LocalDateTime.of(2026, 1, 1, 11, 0))
                        .lastLoginIp("127.0.0.1")
                        .createdAt(Instant.parse("2026-01-01T09:00:00Z"))
                        .updatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                        .build();

        PlatformAdminUser target = mapper.toDomain(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getEmail(), target.getEmail());
        assertEquals(source.getPasswordHash(), target.getPasswordHash());
        assertEquals(source.getFullName(), target.getFullName());
        assertEquals(source.getIsActive(), target.getIsActive());
        assertEquals(source.getFailedLoginAttempts(), target.getFailedLoginAttempts());
        assertEquals(source.getLockedUntil(), target.getLockedUntil());
        assertEquals(source.getLastLoginAt(), target.getLastLoginAt());
        assertEquals(source.getLastLoginIp(), target.getLastLoginIp());
        assertEquals(source.getCreatedAt(), target.getCreatedAt());
        assertEquals(source.getUpdatedAt(), target.getUpdatedAt());
    }

    @Test
    void toEntityMapsAllFields() {
        PlatformAdminUser source = PlatformAdminUser.builder()
                .id(20L)
                .email("owner@example.com")
                .passwordHash("pw")
                .fullName("Owner")
                .isActive(false)
                .failedLoginAttempts(4)
                .lockedUntil(LocalDateTime.of(2026, 2, 1, 8, 0))
                .lastLoginAt(LocalDateTime.of(2026, 1, 31, 8, 0))
                .lastLoginIp("10.0.0.1")
                .createdAt(Instant.parse("2026-01-31T06:00:00Z"))
                .updatedAt(Instant.parse("2026-01-31T07:00:00Z"))
                .build();

        com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser target = mapper.toEntity(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getEmail(), target.getEmail());
        assertEquals(source.getPasswordHash(), target.getPasswordHash());
        assertEquals(source.getFullName(), target.getFullName());
        assertEquals(source.getIsActive(), target.getIsActive());
        assertEquals(source.getFailedLoginAttempts(), target.getFailedLoginAttempts());
        assertEquals(source.getLockedUntil(), target.getLockedUntil());
        assertEquals(source.getLastLoginAt(), target.getLastLoginAt());
        assertEquals(source.getLastLoginIp(), target.getLastLoginIp());
        assertEquals(source.getCreatedAt(), target.getCreatedAt());
        assertEquals(source.getUpdatedAt(), target.getUpdatedAt());
    }
}
