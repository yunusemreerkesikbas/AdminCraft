package com.backend.application.service;

import com.backend.presentation.dto.request.UpdateTenantLanguagesRequest;
import com.backend.presentation.dto.response.LanguageCatalogItem;
import com.backend.presentation.dto.response.TenantLanguagesResponse;

import java.util.List;
import java.util.Set;
import java.util.Map;

public interface LanguageService {

  List<LanguageCatalogItem> getPlatformLanguages();

  TenantLanguagesResponse getTenantLanguages(Long tenantId);

  TenantLanguagesResponse updateTenantLanguages(Long tenantId,
      UpdateTenantLanguagesRequest request);

  Set<com.backend.domain.enums.Language> getSupportedLanguages(Long tenantId);

  void validateTranslationKeys(Long tenantId, Map<String, ?> translationsMap);

  com.backend.domain.enums.Language resolveEffectiveLanguage(Long tenantId,
      com.backend.domain.enums.Language requested);
}
