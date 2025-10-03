package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.TenantLanguagesUpdateRequest;
import com.backend.presentation.dto.response.TenantLanguagesResponse;

import java.util.Set;

public interface TenantLanguageService {
    TenantLanguagesResponse getLanguages(Long tenantId);
    TenantLanguagesResponse updateLanguages(Long tenantId, TenantLanguagesUpdateRequest request);
    Set<Language> getNewlyAddedLanguages(Long tenantId, Set<Language> newSupportedLanguages);
}
