package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TranslationService {

  void upsertTranslations(Component component,
      Map<Language, ComponentRequest.I18nPayload> translations);

  /**
   * Finds translations for multiple components and a single language.
   * SECURITY: Validates that all component IDs belong to the specified tenant.
   *
   * @param componentIds List of component IDs
   * @param language Target language
   * @param tenantId Tenant ID for security validation
   * @return Map of component ID to ComponentTranslation
   * @throws com.backend.domain.exception.TenantSecurityException if any component doesn't belong to tenant
   */
  Map<Long, ComponentTranslation> findByComponentIdsAndLanguage(List<Long> componentIds,
      Language language, Long tenantId);

  /**
   * Finds translations for a single component and multiple languages.
   * SECURITY: Validates that the component ID belongs to the specified tenant.
   *
   * @param componentId Component ID
   * @param languages Set of target languages
   * @param tenantId Tenant ID for security validation
   * @return Map of Language to ComponentTranslation
   * @throws com.backend.domain.exception.TenantSecurityException if component doesn't belong to tenant
   */
  Map<Language, ComponentTranslation> findByComponentIdAndLanguages(Long componentId,
      Set<Language> languages, Long tenantId);

  /**
   * Batch query to fetch translations for multiple components and multiple languages.
   * This method solves the N+1 query problem by fetching all required translations in a single query.
   * SECURITY: Validates that all component IDs belong to the specified tenant.
   *
   * @param componentIds List of component IDs to fetch translations for
   * @param languages Set of languages to fetch translations for
   * @param tenantId Tenant ID for security validation
   * @return Map where key is componentId and value is Map of language to ComponentTranslation
   * @throws com.backend.domain.exception.TenantSecurityException if any component doesn't belong to tenant
   */
  Map<Long, Map<Language, ComponentTranslation>> findByComponentIdsAndLanguages(
      List<Long> componentIds, Set<Language> languages, Long tenantId);

  ComponentTranslation getForLanguageWithFallback(Long componentId,
      Long tenantId,
      Language requested);

  /**
   * Deletes all translations for a component.
   * SECURITY: Validates that the component ID belongs to the specified tenant.
   *
   * @param componentId Component ID
   * @param tenantId Tenant ID for security validation
   * @throws com.backend.domain.exception.TenantSecurityException if component doesn't belong to tenant
   */
  void deleteByComponentId(Long componentId, Long tenantId);

}