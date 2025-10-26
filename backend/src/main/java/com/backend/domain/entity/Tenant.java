package com.backend.domain.entity;

import com.backend.domain.converter.LanguageSetConverter;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.exception.TenantCannotBeActivatedException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "validation.subdomain.required")
    @Size(min = 3, max = 50, message = "validation.subdomain.size")
    @Column(unique = true, nullable = false)
    private String subdomain;
    
    @NotBlank(message = "validation.company.name.required")
    @Size(max = 100, message = "validation.company.name.size")
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @NotBlank(message = "validation.database.name.required")
    @Size(max = 50, message = "validation.database.name.size")
    @Column(name = "database_name", unique = true, nullable = false)
    private String databaseName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "default_language", nullable = false)
    private Language defaultLanguage = Language.TR;

    @Convert(converter = LanguageSetConverter.class)
    @Column(name = "supported_languages", columnDefinition = "JSON")
    private Set<Language> supportedLanguages = new HashSet<>(Set.of(Language.TR));

    @Size(max = 100, message = "validation.custom.domain.size")
    @Column(name = "custom_domain")
    private String customDomain;

    @Column(name = "storage_used_mb")
    private Long storageUsedMb = 0L;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "last_backup_at")
    private LocalDateTime lastBackupAt;
    
    @Size(max = 1000, message = "validation.notes.size")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = "tenant_" + subdomain + "_db";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean canBeActivated() {
        return status == TenantStatus.PENDING;
    }
    
    public void activate() {
        if (!canBeActivated()) {
            throw new TenantCannotBeActivatedException(
                "Tenant with status " + status + " cannot be activated");
        }
        this.status = TenantStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }
    
    public boolean canBeSuspended() {
        return status == TenantStatus.ACTIVE;
    }
    
    public void suspend() {
        if (!canBeSuspended()) {
            throw new IllegalStateException(
                "Tenant with status " + status + " cannot be suspended");
        }
        this.status = TenantStatus.SUSPENDED;
    }
    
    public void setMaintenance() {
        this.status = TenantStatus.MAINTENANCE;
    }
    
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
    
    public boolean supportsLanguage(Language language) {
        return supportedLanguages.contains(language);
    }
    
    public void addSupportedLanguage(Language language) {
        supportedLanguages.add(language);
    }
    
    public void removeSupportedLanguage(Language language) {
        if (language.equals(defaultLanguage)) {
            throw new IllegalArgumentException("Cannot remove default language");
        }
        supportedLanguages.remove(language);
    }
    
    public String getFullDomain() {
        return customDomain != null ? customDomain : subdomain + ".admincraft.com";
    }
}