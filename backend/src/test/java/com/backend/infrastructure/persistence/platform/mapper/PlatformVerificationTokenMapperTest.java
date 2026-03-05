package com.backend.infrastructure.persistence.platform.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.entity.PlatformVerificationToken;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;

class PlatformVerificationTokenMapperTest {

    private final PlatformVerificationTokenMapper mapper =
            new PlatformVerificationTokenMapper(new PlatformAdminUserMapper());

    @Test
    void toDomainMapsTokenAndNestedAdmin() {
        com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser adminEntity =
                com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser.builder()
                        .id(5L)
                        .email("admin@example.com")
                        .passwordHash("hash")
                        .fullName("Admin")
                        .isActive(true)
                        .failedLoginAttempts(0)
                        .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                        .updatedAt(Instant.parse("2026-01-01T01:00:00Z"))
                        .build();

        com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken source =
                com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken.builder()
                        .id(9L)
                        .adminUser(adminEntity)
                        .tokenHash("token-hash")
                        .tokenType(TokenType.LOGIN_OTP)
                        .status(TokenStatus.ACTIVE)
                        .targetValue("target")
                        .expiresAt(LocalDateTime.of(2026, 1, 1, 2, 0))
                        .attemptCount(1)
                        .maxAttempts(5)
                        .usedAt(LocalDateTime.of(2026, 1, 1, 1, 30))
                        .ipAddress("127.0.0.1")
                        .userAgent("JUnit")
                        .createdAt(LocalDateTime.of(2026, 1, 1, 0, 30))
                        .build();

        PlatformVerificationToken target = mapper.toDomain(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getTokenHash(), target.getTokenHash());
        assertEquals(source.getTokenType(), target.getTokenType());
        assertEquals(source.getStatus(), target.getStatus());
        assertEquals(source.getTargetValue(), target.getTargetValue());
        assertEquals(source.getExpiresAt(), target.getExpiresAt());
        assertEquals(source.getAttemptCount(), target.getAttemptCount());
        assertEquals(source.getMaxAttempts(), target.getMaxAttempts());
        assertEquals(source.getUsedAt(), target.getUsedAt());
        assertEquals(source.getIpAddress(), target.getIpAddress());
        assertEquals(source.getUserAgent(), target.getUserAgent());
        assertEquals(source.getCreatedAt(), target.getCreatedAt());
        assertNotNull(target.getAdminUser());
        assertEquals(adminEntity.getId(), target.getAdminUser().getId());
        assertEquals(adminEntity.getEmail(), target.getAdminUser().getEmail());
    }

    @Test
    void toEntityMapsTokenAndNestedAdmin() {
        PlatformAdminUser adminDomain = PlatformAdminUser.builder()
                .id(11L)
                .email("owner@example.com")
                .passwordHash("pw")
                .fullName("Owner")
                .isActive(true)
                .failedLoginAttempts(1)
                .createdAt(Instant.parse("2026-02-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-02-01T01:00:00Z"))
                .build();

        PlatformVerificationToken source = PlatformVerificationToken.builder()
                .id(12L)
                .adminUser(adminDomain)
                .tokenHash("hash-2")
                .tokenType(TokenType.PASSWORD_RESET)
                .status(TokenStatus.USED)
                .targetValue("mail")
                .expiresAt(LocalDateTime.of(2026, 2, 1, 2, 0))
                .attemptCount(3)
                .maxAttempts(7)
                .usedAt(LocalDateTime.of(2026, 2, 1, 1, 30))
                .ipAddress("10.0.0.1")
                .userAgent("Agent")
                .createdAt(LocalDateTime.of(2026, 2, 1, 0, 30))
                .build();

        com.backend.infrastructure.persistence.platform.entity.PlatformVerificationToken target = mapper.toEntity(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getTokenHash(), target.getTokenHash());
        assertEquals(source.getTokenType(), target.getTokenType());
        assertEquals(source.getStatus(), target.getStatus());
        assertEquals(source.getTargetValue(), target.getTargetValue());
        assertEquals(source.getExpiresAt(), target.getExpiresAt());
        assertEquals(source.getAttemptCount(), target.getAttemptCount());
        assertEquals(source.getMaxAttempts(), target.getMaxAttempts());
        assertEquals(source.getUsedAt(), target.getUsedAt());
        assertEquals(source.getIpAddress(), target.getIpAddress());
        assertEquals(source.getUserAgent(), target.getUserAgent());
        assertEquals(source.getCreatedAt(), target.getCreatedAt());
        assertNotNull(target.getAdminUser());
        assertEquals(adminDomain.getId(), target.getAdminUser().getId());
        assertEquals(adminDomain.getEmail(), target.getAdminUser().getEmail());
    }
}
