package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record TenantResponse(
    Long id,
    String subdomain,
    String companyName,
    String databaseName,
    TenantStatus status,
    String statusDisplay,
    Language defaultLanguage,
    String defaultLanguageDisplay,
    Set<Language> supportedLanguages,
    String adminEmail,
    String adminName,
    String phone,
    String customDomain,
    Boolean sslEnabled,
    String timezone,
    String currency,
    Long storageUsedMb,
    String fullDomain,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime activatedAt,
    LocalDateTime lastBackupAt,
    String notes
) {
    public static TenantResponse from(com.backend.domain.entity.Tenant tenant, Language displayLanguage) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getSubdomain(),
            tenant.getCompanyName(),
            tenant.getDatabaseName(),
            tenant.getStatus(),
            tenant.getStatus().getDisplayName(displayLanguage),
            tenant.getDefaultLanguage(),
            tenant.getDefaultLanguage().getEnglishName(),
            tenant.getSupportedLanguages(),
            tenant.getAdminEmail(),
            tenant.getAdminName(),
            tenant.getPhone(),
            tenant.getCustomDomain(),
            tenant.getSslEnabled(),
            tenant.getTimezone(),
            tenant.getCurrency(),
            tenant.getStorageUsedMb(),
            tenant.getFullDomain(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt(),
            tenant.getActivatedAt(),
            tenant.getLastBackupAt(),
            tenant.getNotes()
        );
    }
}