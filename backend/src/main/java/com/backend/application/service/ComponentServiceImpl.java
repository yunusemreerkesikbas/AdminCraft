package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.exception.ComponentConflictException;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.exception.ComponentNotFoundException;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.SiteComponentResponse;
import com.backend.presentation.mapper.ComponentMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComponentServiceImpl implements ComponentService {

  private final ComponentRepository componentRepository;
  private final ComponentTranslationRepository translationRepository;
  private final TenantRepository tenantRepository;
  private final LanguageService languageService;
  private final TranslationService translationService;

  public ComponentServiceImpl(ComponentRepository componentRepository,
      ComponentTranslationRepository translationRepository,
      TenantRepository tenantRepository,
      LanguageService languageService,
      TranslationService translationService) {
    this.componentRepository = componentRepository;
    this.translationRepository = translationRepository;
    this.tenantRepository = tenantRepository;
    this.languageService = languageService;
    this.translationService = translationService;
  }

  @Override
  @Transactional
  public ComponentResponse create(Long tenantId, ComponentRequest request) {
    componentRepository.findByTenantAndTypeAndKey(tenantId, request.type(), request.key())
        .ifPresent(c -> {
          throw new ComponentConflictException("ui.component.key.conflict");
        });

    Component component = new Component();
    component.setTenantId(tenantId);
    component.setType(request.type());
    component.setKey(request.key());
    if (request.status() != null)
      component.setStatus(request.status());
    if (request.visible() != null)
      component.setVisible(request.visible());
    if (request.sortOrder() != null)
      component.setSortOrder(request.sortOrder());
    component.setCreatedBy(com.backend.shared.common.SecurityUtil.getCurrentUserIdOrThrow());

    languageService.validateTranslationKeys(tenantId, request.translations());

    var saved = componentRepository.save(component);

    // Map<String, I18nPayload> -> Map<Language, I18nPayload>
    java.util.Map<com.backend.domain.enums.Language, ComponentRequest.I18nPayload> map = new java.util.HashMap<>();
    for (var e : request.translations().entrySet()) {
      var lang = com.backend.domain.enums.Language.fromCode(e.getKey())
          .orElseThrow(() -> new IllegalArgumentException("language.invalid"));
      map.put(lang, e.getValue());
    }
    translationService.upsertTranslations(saved, map);

    // SECURITY: Use secure method with tenant validation
    var langsMap = translationService.findByComponentIdAndLanguages(
        saved.getId(), languageService.getSupportedLanguages(tenantId), tenantId);
    return ComponentMapper.toResponse(saved, langsMap);
  }

  @Override
  @Transactional
  public ComponentResponse update(Long id, Long tenantId, ComponentRequest request) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    if (request.status() != null)
      component.setStatus(request.status());
    if (request.visible() != null)
      component.setVisible(request.visible());
    if (request.sortOrder() != null)
      component.setSortOrder(request.sortOrder());
    component.setUpdatedBy(com.backend.shared.common.SecurityUtil.getCurrentUserIdOrThrow());

    languageService.validateTranslationKeys(tenantId, request.translations());

    var saved = componentRepository.save(component);

    java.util.Map<com.backend.domain.enums.Language, ComponentRequest.I18nPayload> map = new java.util.HashMap<>();
    for (var e : request.translations().entrySet()) {
      var lang = com.backend.domain.enums.Language.fromCode(e.getKey())
          .orElseThrow(() -> new IllegalArgumentException("language.invalid"));
      map.put(lang, e.getValue());
    }
    translationService.upsertTranslations(saved, map);

    // SECURITY: Use secure method with tenant validation
    var langsMap = translationService.findByComponentIdAndLanguages(
        saved.getId(), languageService.getSupportedLanguages(tenantId), tenantId);
    return ComponentMapper.toResponse(saved, langsMap);
  }

  @Override
  @Transactional
  public void delete(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));
    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    // SECURITY: Use secure delete with tenant validation
    translationService.deleteByComponentId(component.getId(), tenantId);
    componentRepository.delete(component);
  }

  @Override
  public ComponentResponse get(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    // SECURITY: Use secure method with tenant validation
    var langsMap = translationService.findByComponentIdAndLanguages(
        id, languageService.getSupportedLanguages(tenantId), tenantId);
    return ComponentMapper.toResponse(component, langsMap);
  }

  @Override
  public List<ComponentResponse> list(Long tenantId) {
    List<Component> components = componentRepository.findAllByTenantId(tenantId);
    if (components.isEmpty()) {
      return List.of();
    }

    // PERFORMANCE FIX: Use batch query with tenant validation
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .collect(Collectors.toList());

    var supportedLanguages = languageService.getSupportedLanguages(tenantId);

    // SECURITY: Use secure batch query with tenant validation
    Map<Long, Map<Language, ComponentTranslation>> translationsMap =
        translationService.findByComponentIdsAndLanguages(componentIds, supportedLanguages, tenantId);

    return components.stream()
        .map(c -> ComponentMapper.toResponse(c, translationsMap.get(c.getId())))
        .collect(Collectors.toList());
  }

  @Override
  public List<ComponentResponse> list(Long tenantId, ComponentListFilter filter) {
    List<Component> components;
    if (filter != null && filter.type() != null && filter.status() != null) {
      components = componentRepository.findAllByTenantIdAndTypeAndStatus(tenantId, filter.type(), filter.status());
    } else if (filter != null && filter.type() != null) {
      components = componentRepository.findAllByTenantIdAndType(tenantId, filter.type());
    } else {
      components = componentRepository.findAllByTenantId(tenantId);
    }

    if (components.isEmpty())
      return List.of();

    // PERFORMANCE FIX: Use batch query with tenant validation
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .collect(Collectors.toList());

    var supportedLanguages = languageService.getSupportedLanguages(tenantId);

    // SECURITY: Use secure batch query with tenant validation
    Map<Long, Map<Language, ComponentTranslation>> translationsMap =
        translationService.findByComponentIdsAndLanguages(componentIds, supportedLanguages, tenantId);

    return components.stream()
        .map(c -> ComponentMapper.toResponse(c, translationsMap.get(c.getId())))
        .collect(Collectors.toList());
  }

  @Override
  public List<SiteComponentResponse> getSiteComponents(Long tenantId, ComponentType type, Language language) {
    Language effectiveLanguage = languageService.resolveEffectiveLanguage(tenantId, language);
    List<Component> components = componentRepository.findActiveVisibleByTenantIdAndType(tenantId, type);

    if (components.isEmpty()) {
      return List.of();
    }
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .collect(Collectors.toList());

    // SECURITY: Use secure method with tenant validation
    Map<Long, ComponentTranslation> translationMap = translationService
        .findByComponentIdsAndLanguage(componentIds, effectiveLanguage, tenantId);

    return components.stream()
        .map(component -> ComponentMapper.toSiteResponse(component, translationMap.get(component.getId())))
        .collect(Collectors.toList());
  }

}