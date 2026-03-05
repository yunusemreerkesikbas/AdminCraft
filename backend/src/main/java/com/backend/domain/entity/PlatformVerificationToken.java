package com.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformVerificationToken {

    private Long id;
    private PlatformAdminUser adminUser;
    private String tokenHash;
    private TokenType tokenType;

    @Builder.Default
    private TokenStatus status = TokenStatus.ACTIVE;

    private String targetValue;
    private LocalDateTime expiresAt;

    @Builder.Default
    private Integer attemptCount = 0;

    @Builder.Default
    private Integer maxAttempts = 5;

    private LocalDateTime usedAt;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return status == TokenStatus.ACTIVE && !isExpired() && attemptCount < maxAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(0, maxAttempts - attemptCount);
    }

    public void incrementAttempts() {
        attemptCount++;
        if (attemptCount >= maxAttempts) {
            status = TokenStatus.REVOKED;
        }
    }

    public void markAsUsed() {
        status = TokenStatus.USED;
        usedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformVerificationToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
