package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import java.util.Set;

public record TenantLanguagesResponse(
        Long tenantId,
        Language defaultLanguage,
        Set<Language> supportedLanguages) {

    public static TenantLanguagesResponse from(com.backend.domain.entity.Tenant tenant) {
        return new TenantLanguagesResponse(
                tenant.getId(),
                tenant.getDefaultLanguage(),
                tenant.getSupportedLanguages());
    }
}
