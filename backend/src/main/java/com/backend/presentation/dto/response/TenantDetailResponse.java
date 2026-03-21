package com.backend.presentation.dto.response;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record TenantDetailResponse(
        Long id,
        String subdomain,
        String companyName,
        TenantStatus status,
        Language defaultLanguage,
        Set<LanguageResponse> supportedLanguages,
        Currency currency,
        String provisioningStatus,
        Integer provisionedModulesCount,

        LocalDateTime createdAt,
        LocalDateTime activatedAt,

        String customDomain,

        String databaseName,
        Long storageUsedMb,
        String fullDomain,
        LocalDateTime updatedAt,
        LocalDateTime lastBackupAt,
        String notes) {

    public static TenantDetailResponse from(
            com.backend.domain.entity.Tenant tenant,
            Language displayLanguage,
            ProvisioningStatus provisioningStatus,
            Integer modulesCount,
            String fullDomain) {
        return new TenantDetailResponse(
                tenant.getId(),
                tenant.getSubdomain(),
                tenant.getCompanyName(),
                tenant.getStatus(),
                tenant.getDefaultLanguage(),
                tenant.getSupportedLanguages().stream()
                        .map(LanguageResponse::from)
                        .collect(Collectors.toSet()),
                tenant.getCurrency(),
                provisioningStatus.name().toLowerCase(),
                modulesCount,
                tenant.getCreatedAt(),
                tenant.getActivatedAt(),
                tenant.getCustomDomain(),
                tenant.getDatabaseName(),
                tenant.getStorageUsedMb(),
                fullDomain,
                tenant.getUpdatedAt(),
                tenant.getLastBackupAt(),
                tenant.getNotes());
    }
}
