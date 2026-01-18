package com.backend.presentation.dto.response;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record TenantListResponse(
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

        String customDomain) {

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
                tenant.getDefaultLanguage(),
                tenant.getSupportedLanguages().stream()
                        .map(LanguageResponse::from)
                        .collect(Collectors.toSet()),
                tenant.getCurrency(),
                provisioningStatus.name().toLowerCase(),
                modulesCount,
                tenant.getCreatedAt(),
                tenant.getActivatedAt(),
                tenant.getCustomDomain());
    }
}
