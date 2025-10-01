package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.exception.TenantSecurityException;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.ComponentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TranslationServiceImpl implements TranslationService {

  private static final Logger logger = LoggerFactory.getLogger(TranslationServiceImpl.class);

  private final ComponentTranslationRepository translationRepository;
  private final TenantRepository tenantRepository;
  private final ComponentRepository componentRepository;

  public TranslationServiceImpl(ComponentTranslationRepository translationRepository,
      TenantRepository tenantRepository,
      ComponentRepository componentRepository) {
    this.translationRepository = translationRepository;
    this.tenantRepository = tenantRepository;
    this.componentRepository = componentRepository;
  }

  @Override
  public void upsertTranslations(Component component,
      Map<Language, ComponentRequest.I18nPayload> translations) {
    if (translations == null || translations.isEmpty()) {
      return;
    }

    Long componentId = component.getId();
    Long tenantId = component.getTenantId();

    // SECURITY: Validate component belongs to tenant (extra safety check)
    if (!component.isValidForTenant(tenantId)) {
      logger.error("SECURITY VIOLATION: Component {} does not belong to tenant {} during translation upsert",
                   componentId, tenantId);
      throw TenantSecurityException.invalidComponentAccess(componentId, tenantId);
    }

    for (Map.Entry<Language, ComponentRequest.I18nPayload> e : translations.entrySet()) {
      Language lang = e.getKey();
      ComponentRequest.I18nPayload payload = e.getValue();
      ComponentTranslation t = translationRepository
          .findByComponentIdAndLanguage(componentId, lang)
          .orElseGet(() -> {
            ComponentTranslation nt = new ComponentTranslation();
            nt.setComponent(component);
            nt.setLanguage(lang);
            return nt;
          });
      if (payload != null) {
        t.setTitle(payload.title());
        t.setSubtitle(payload.subtitle());
        t.setData(payload.data());
      }
      translationRepository.save(t);
    }

    logger.debug("Updated translations for component {} in tenant {} for languages: {}",
                 componentId, tenantId, translations.keySet());
  }

  // =======================================================================================
  // SECURE METHODS: All methods include tenant validation
  // =======================================================================================

  @Override
  public Map<Long, ComponentTranslation> findByComponentIdsAndLanguage(List<Long> componentIds,
      Language language, Long tenantId) {
    if (componentIds == null || componentIds.isEmpty()) {
      return Map.of();
    }

    // SECURITY: Validate all components belong to the tenant
    validateComponentsBelongToTenant(componentIds, tenantId);

    List<ComponentTranslation> list = translationRepository
        .findAllByComponentIdInAndLanguage(componentIds, language);

    logger.debug("Found {} translations for {} components in language {} for tenant {}",
                 list.size(), componentIds.size(), language, tenantId);

    return list.stream().collect(Collectors.toMap(t -> t.getComponent().getId(), t -> t));
  }

  @Override
  public Map<Language, ComponentTranslation> findByComponentIdAndLanguages(Long componentId,
      Set<Language> languages, Long tenantId) {
    Map<Language, ComponentTranslation> map = new HashMap<>();
    if (languages == null || languages.isEmpty()) {
      return map;
    }

    // SECURITY: Validate component belongs to the tenant
    validateComponentBelongsToTenant(componentId, tenantId);

    for (Language lang : languages) {
      translationRepository.findByComponentIdAndLanguage(componentId, lang)
          .ifPresent(t -> map.put(lang, t));
    }

    logger.debug("Found {} translations for component {} in tenant {} for languages: {}",
                 map.size(), componentId, tenantId, languages);

    return map;
  }

  @Override
  public Map<Long, Map<Language, ComponentTranslation>> findByComponentIdsAndLanguages(
      List<Long> componentIds, Set<Language> languages, Long tenantId) {
    Map<Long, Map<Language, ComponentTranslation>> result = new HashMap<>();

    if (componentIds == null || componentIds.isEmpty() || languages == null || languages.isEmpty()) {
      return result;
    }

    // SECURITY: Validate all components belong to the tenant
    validateComponentsBelongToTenant(componentIds, tenantId);

    // Initialize nested maps for all component IDs
    for (Long componentId : componentIds) {
      result.put(componentId, new HashMap<>());
    }

    // Fetch all translations in a single batch query
    List<Language> languageList = languages.stream().toList();
    List<ComponentTranslation> translations = translationRepository
        .findAllByComponentIdInAndLanguageIn(componentIds, languageList);

    // Group translations by component ID and language
    for (ComponentTranslation translation : translations) {
      Long componentId = translation.getComponent().getId();
      Language language = translation.getLanguage();
      result.get(componentId).put(language, translation);
    }

    logger.debug("Found {} total translations for {} components in tenant {} for languages: {}",
                 translations.size(), componentIds.size(), tenantId, languages);

    return result;
  }

  @Override
  public ComponentTranslation getForLanguageWithFallback(Long componentId,
      Long tenantId,
      Language requested) {
    // SECURITY: Validate component belongs to the tenant
    validateComponentBelongsToTenant(componentId, tenantId);

    var tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new TenantNotFoundException(tenantId));
    Language effective = tenant.getSupportedLanguages().contains(requested)
        ? requested
        : tenant.getDefaultLanguage();

    ComponentTranslation translation = translationRepository.findByComponentIdAndLanguage(componentId, effective)
        .orElse(null);

    logger.debug("Retrieved translation for component {} in tenant {} for language {} (requested: {})",
                 componentId, tenantId, effective, requested);

    return translation;
  }

  @Override
  public void deleteByComponentId(Long componentId, Long tenantId) {
    // SECURITY: Validate component belongs to the tenant
    validateComponentBelongsToTenant(componentId, tenantId);

    translationRepository.deleteByComponentId(componentId);

    logger.info("Deleted all translations for component {} in tenant {}", componentId, tenantId);
  }

  // =======================================================================================
  // DEPRECATED METHODS: Legacy methods without tenant validation - SECURITY RISK
  // These methods should be replaced with secure versions
  // =======================================================================================

  @Override
  @Deprecated(since = "1.0", forRemoval = true)
  public Map<Long, ComponentTranslation> findByComponentIdsAndLanguage(List<Long> componentIds,
      Language language) {
    logger.warn("DEPRECATED METHOD CALL: findByComponentIdsAndLanguage without tenant validation. " +
                "This poses a security risk. Component IDs: {}", componentIds);

    if (componentIds == null || componentIds.isEmpty()) {
      return Map.of();
    }
    List<ComponentTranslation> list = translationRepository
        .findAllByComponentIdInAndLanguage(componentIds, language);
    return list.stream().collect(Collectors.toMap(t -> t.getComponent().getId(), t -> t));
  }

  @Override
  @Deprecated(since = "1.0", forRemoval = true)
  public Map<Language, ComponentTranslation> findByComponentIdAndLanguages(Long componentId,
      Set<Language> languages) {
    logger.warn("DEPRECATED METHOD CALL: findByComponentIdAndLanguages without tenant validation. " +
                "This poses a security risk. Component ID: {}", componentId);

    Map<Language, ComponentTranslation> map = new HashMap<>();
    if (languages == null || languages.isEmpty()) {
      return map;
    }
    for (Language lang : languages) {
      translationRepository.findByComponentIdAndLanguage(componentId, lang)
          .ifPresent(t -> map.put(lang, t));
    }
    return map;
  }

  @Override
  @Deprecated(since = "1.0", forRemoval = true)
  public Map<Long, Map<Language, ComponentTranslation>> findByComponentIdsAndLanguages(
      List<Long> componentIds, Set<Language> languages) {
    logger.warn("DEPRECATED METHOD CALL: findByComponentIdsAndLanguages without tenant validation. " +
                "This poses a security risk. Component IDs: {}", componentIds);

    Map<Long, Map<Language, ComponentTranslation>> result = new HashMap<>();

    if (componentIds == null || componentIds.isEmpty() || languages == null || languages.isEmpty()) {
      return result;
    }

    // Initialize nested maps for all component IDs
    for (Long componentId : componentIds) {
      result.put(componentId, new HashMap<>());
    }

    // Fetch all translations in a single batch query
    List<Language> languageList = languages.stream().toList();
    List<ComponentTranslation> translations = translationRepository
        .findAllByComponentIdInAndLanguageIn(componentIds, languageList);

    // Group translations by component ID and language
    for (ComponentTranslation translation : translations) {
      Long componentId = translation.getComponent().getId();
      Language language = translation.getLanguage();
      result.get(componentId).put(language, translation);
    }

    return result;
  }

  @Override
  @Deprecated(since = "1.0", forRemoval = true)
  public void deleteByComponentId(Long componentId) {
    logger.warn("DEPRECATED METHOD CALL: deleteByComponentId without tenant validation. " +
                "This poses a security risk. Component ID: {}", componentId);

    translationRepository.deleteByComponentId(componentId);
  }

  // =======================================================================================
  // PRIVATE SECURITY HELPER METHODS
  // =======================================================================================

  /**
   * Validates that a single component belongs to the specified tenant.
   * Throws TenantSecurityException if validation fails.
   */
  private void validateComponentBelongsToTenant(Long componentId, Long tenantId) {
    try {
      componentRepository.validateComponentBelongsToTenant(componentId, tenantId);
    } catch (Exception e) {
      logger.error("SECURITY VIOLATION: Component {} validation failed for tenant {}. Error: {}",
                   componentId, tenantId, e.getMessage());
      throw TenantSecurityException.invalidComponentAccess(componentId, tenantId);
    }
  }

  /**
   * Validates that all components in the list belong to the specified tenant.
   * Throws TenantSecurityException if any validation fails.
   */
  private void validateComponentsBelongToTenant(List<Long> componentIds, Long tenantId) {
    try {
      componentRepository.validateComponentsBelongToTenant(componentIds, tenantId);
    } catch (Exception e) {
      logger.error("SECURITY VIOLATION: Component batch validation failed for tenant {}. " +
                   "Component IDs: {}, Error: {}", tenantId, componentIds, e.getMessage());
      throw TenantSecurityException.invalidBatchComponentAccess(tenantId);
    }
  }
}