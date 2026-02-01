package com.backend.infrastructure.persistence.platform.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(schema = "platform_management", name = "platform_admin_users")
public class PlatformAdminUser {

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCK_DURATION_MINUTES = 30;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Builder.Default
  @Column(name = "failed_login_attempts", nullable = false)
  private Integer failedLoginAttempts = 0;

  @Column(name = "locked_until")
  private LocalDateTime lockedUntil;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "last_login_ip", length = 45)
  private String lastLoginIp;

  @Column(name = "created_at", updatable = false, insertable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false)
  private Instant updatedAt;

  // Login tracking methods (matches User entity pattern)

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
