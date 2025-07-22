package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email(message = "validation.email.invalid")
    @NotBlank(message = "validation.email.required")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "validation.password.required")
    @Size(min = 60, max = 60, message = "validation.password.hash.size")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotBlank(message = "validation.full.name.required")
    @Size(max = 100, message = "validation.full.name.size")
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Size(max = 50, message = "validation.first.name.size")
    @Column(name = "first_name")
    private String firstName;
    
    @Size(max = 50, message = "validation.last.name.size")
    @Column(name = "last_name")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.VIEWER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", nullable = false)
    private Language preferredLanguage = Language.TR;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Size(max = 20, message = "validation.phone.size")
    private String phone;
    
    @Size(max = 255, message = "validation.avatar.url.size")
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Size(max = 100, message = "validation.job.title.size")
    @Column(name = "job_title")
    private String jobTitle;
    
    @Size(max = 100, message = "validation.department.size")
    private String department;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Size(max = 500, message = "validation.notes.size")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        passwordChangedAt = LocalDateTime.now();
        if (fullName == null && firstName != null && lastName != null) {
            fullName = firstName + " " + lastName;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean canAccessContent(Content content) {
        return this.tenantId.equals(content.getTenantId()) &&
               this.role.hasPermission(UserRole.Permission.READ_CONTENT);
    }
    
    public boolean canEditContent(Content content) {
        return this.tenantId.equals(content.getTenantId()) &&
               this.role.hasPermission(UserRole.Permission.WRITE_CONTENT);
    }
    
    public boolean canDeleteContent(Content content) {
        return this.tenantId.equals(content.getTenantId()) &&
               this.role.hasPermission(UserRole.Permission.DELETE_CONTENT);
    }
    
    public boolean canManageUsers() {
        return this.role.hasPermission(UserRole.Permission.WRITE_USER);
    }
    
    public boolean canManageTenant() {
        return this.role.hasPermission(UserRole.Permission.MANAGE_TENANT);
    }
    
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public boolean canLogin() {
        return isActive && !isAccountLocked() && emailVerified;
    }
    
    public void recordFailedLogin() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            lockedUntil = LocalDateTime.now().plusMinutes(30); // Lock for 30 minutes
        }
    }
    
    public void recordSuccessfulLogin(String ipAddress) {
        lastLoginAt = LocalDateTime.now();
        lastLoginIp = ipAddress;
        failedLoginAttempts = 0;
        lockedUntil = null;
    }
    
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
    
    public boolean isTenantAdmin() {
        return role == UserRole.TENANT_ADMIN;
    }
    
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return email;
    }
}