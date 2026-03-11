package com.backend.domain.entity;

import java.time.Instant;
import java.time.LocalDateTime;

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
public class PlatformAdminUser {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public int getRemainingLockMinutes() {
        if (lockedUntil == null || !isAccountLocked()) {
            return 0;
        }
        long seconds = java.time.Duration.between(LocalDateTime.now(), lockedUntil).getSeconds();
        return (int) Math.max(0, (seconds + 59) / 60);
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
        }
    }

    public void recordSuccessfulLogin(String ip) {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ip;
    }
}
