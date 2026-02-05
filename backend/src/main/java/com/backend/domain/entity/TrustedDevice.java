package com.backend.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trusted_devices",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_fingerprint"}, name = "uk_user_device")
    },
    indexes = {
        @Index(columnList = "user_id", name = "idx_device_user"),
        @Index(columnList = "expires_at", name = "idx_device_expires")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 64)
    @Column(name = "device_fingerprint", nullable = false, length = 64)
    private String deviceFingerprint;

    @Size(max = 100)
    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Size(max = 50)
    @Column(length = 50)
    private String browser;

    @Size(max = 50)
    @Column(length = 50)
    private String os;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "trusted_at", nullable = false, updatable = false)
    private LocalDateTime trustedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        trustedAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUsedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    public void updateLastUsed() {
        lastUsedAt = LocalDateTime.now();
    }

    public void extendExpiry(int days) {
        expiresAt = LocalDateTime.now().plusDays(days);
    }

    public long getRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }
}
