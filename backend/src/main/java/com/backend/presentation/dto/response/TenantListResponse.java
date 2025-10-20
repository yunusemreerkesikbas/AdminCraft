package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record TenantListResponse(
        Long id,
        String subdomain,
        String companyName,
        TenantStatus status,
        String statusDisplay,
        Language defaultLanguage,
        String defaultLanguageDisplay,
        Set<Language> supportedLanguages,

        String provisioningStatus,
        Integer provisionedModulesCount,

        LocalDateTime createdAt,
        LocalDateTime activatedAt,

        String customDomain,
        Boolean sslEnabled) {

    public static TenantListResponse from(
            com.backend.domain.entity.Tenant tenant,
            Language displayLanguage,
            ProvisioningStatus provisioningStatus,
            Integer modulesCount) {
        return new TenantListResponse(
                tenant.getId(),
                tenant.getSubdomain(),
                tenant.getCompanyName(),
                tenant.getStatus(),
                tenant.getStatus().getDisplayName(displayLanguage),
                tenant.getDefaultLanguage(),
                tenant.getDefaultLanguage().getEnglishName(),
                tenant.getSupportedLanguages(),
                provisioningStatus.name().toLowerCase(),
                modulesCount,
                tenant.getCreatedAt(),
                tenant.getActivatedAt(),
                tenant.getCustomDomain(),
                tenant.getSslEnabled());
    }
}
