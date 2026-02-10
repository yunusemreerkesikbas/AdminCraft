package com.backend.infrastructure.persistence.platform.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "platform_management", name = "platform_verification_tokens", indexes = {
        @Index(columnList = "platform_admin_user_id", name = "idx_platform_token_admin_user"),
        @Index(columnList = "token_hash", name = "uk_platform_verification_token_hash"),
        @Index(columnList = "token_type, status", name = "idx_platform_token_type_status"),
        @Index(columnList = "expires_at", name = "idx_platform_token_expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_admin_user_id", nullable = false)
    private PlatformAdminUser adminUser;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TokenStatus status = TokenStatus.ACTIVE;

    @Column(name = "target_value", length = 255)
    private String targetValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TokenStatus.ACTIVE;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
        if (maxAttempts == null) {
            maxAttempts = 5;
        }
    }

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
}
