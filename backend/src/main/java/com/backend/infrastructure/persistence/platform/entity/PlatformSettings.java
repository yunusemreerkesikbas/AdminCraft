package com.backend.infrastructure.persistence.platform.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.TwoFactorPolicy;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_settings", schema = "platform_management")
@Data
@NoArgsConstructor
public class PlatformSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_name", nullable = false, length = 100)
    private String platformName;

    @Column(name = "default_language", nullable = false, length = 2)
    private String defaultLanguage;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    @Column(name = "email_from_address", nullable = false, length = 255)
    private String emailFromAddress;

    @Column(name = "email_from_name", nullable = false, length = 100)
    private String emailFromName;

    @Enumerated(EnumType.STRING)
    @Column(name = "two_factor_policy", nullable = false, length = 20)
    private TwoFactorPolicy twoFactorPolicy = TwoFactorPolicy.DISABLED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
