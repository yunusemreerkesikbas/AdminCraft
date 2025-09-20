package com.backend.application.service;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.UpdateTenantLanguagesRequest;
import com.backend.presentation.dto.response.LanguageCatalogItem;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LanguageServiceImpl implements LanguageService {

  private final TenantRepository tenantRepository;

  public LanguageServiceImpl(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  public List<LanguageCatalogItem> getPlatformLanguages() {
    return Arrays.stream(Language.values())
        .map(l -> new LanguageCatalogItem(l.getCode(), l.name()))
        .collect(Collectors.toList());
  }

  @Override
  public TenantLanguagesResponse getTenantLanguages(Long tenantId) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("tenant.not.found"));
    return new TenantLanguagesResponse(
        tenant.getDefaultLanguage().getCode(),
        tenant.getSupportedLanguages().stream().map(Language::getCode).toList());
  }

  @Override
  @Transactional
  public TenantLanguagesResponse updateTenantLanguages(Long tenantId,
      UpdateTenantLanguagesRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("language.update.payload.required");
    }

    var defaultLang = Language.fromCode(request.defaultLanguage())
        .orElseThrow(() -> new IllegalArgumentException("language.invalid"));

    if (request.supported() == null || request.supported().isEmpty()) {
      throw new IllegalArgumentException("language.supported.required");
    }

    Set<Language> supported = request.supported().stream()
        .map(code -> Language.fromCode(code)
            .orElseThrow(() -> new IllegalArgumentException("language.invalid: " + code)))
        .collect(Collectors.toSet());

    supported.add(defaultLang);

    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("tenant.not.found"));

    tenant.setDefaultLanguage(defaultLang);
    tenant.setSupportedLanguages(supported);

    tenantRepository.save(tenant);

    return new TenantLanguagesResponse(
        tenant.getDefaultLanguage().getCode(),
        tenant.getSupportedLanguages().stream().map(Language::getCode).toList());
  }

  @Override
  public Set<Language> getSupportedLanguages(Long tenantId) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("tenant.not.found"));
    return tenant.getSupportedLanguages();
  }

  @Override
  public void validateTranslationKeys(Long tenantId, java.util.Map<String, ?> translationsMap) {
    if (translationsMap == null)
      return;
    Set<Language> supported = getSupportedLanguages(tenantId);
    for (String key : translationsMap.keySet()) {
      Language lang = Language.fromCode(key)
          .orElseThrow(() -> new IllegalArgumentException("language.invalid"));
      if (!supported.contains(lang)) {
        throw new IllegalArgumentException("language.unsupported: " + key);
      }
    }
  }

  @Override
  public Language resolveEffectiveLanguage(Long tenantId, Language requested) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("tenant.not.found"));
    if (requested == null)
      return tenant.getDefaultLanguage();
    return tenant.getSupportedLanguages().contains(requested)
        ? requested
        : tenant.getDefaultLanguage();
  }
}
