package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.exception.ComponentConflictException;
import com.backend.domain.exception.ComponentNotFoundException;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTranslationRepository;
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

  public ComponentServiceImpl(ComponentRepository componentRepository,
      ComponentTranslationRepository translationRepository) {
    this.componentRepository = componentRepository;
    this.translationRepository = translationRepository;
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

    var saved = componentRepository.save(component);

    for (var entry : request.translations().entrySet()) {
      var langCode = entry.getKey();
      var payload = entry.getValue();
      var lang = com.backend.domain.enums.Language.fromCode(langCode)
          .orElseThrow(() -> new IllegalArgumentException("Invalid language code: " + langCode));
      ComponentTranslation t = new ComponentTranslation();
      t.setComponentId(saved.getId());
      t.setLanguage(lang);
      t.setTitle(payload != null ? payload.title() : null);
      t.setSubtitle(payload != null ? payload.subtitle() : null);
      t.setData(payload != null ? payload.data() : null);
      translationRepository.save(t);
    }

    var tr = translationRepository.findByComponentIdAndLanguage(saved.getId(), Language.TR).orElse(null);
    var en = translationRepository.findByComponentIdAndLanguage(saved.getId(), Language.EN).orElse(null);
    return ComponentMapper.toResponse(saved, tr, en);
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

    var saved = componentRepository.save(component);

    for (var entry : request.translations().entrySet()) {
      var lang = Language.fromCode(entry.getKey())
          .orElseThrow(() -> new IllegalArgumentException("Invalid language code: " + entry.getKey()));
      var payload = entry.getValue();
      ComponentTranslation t = translationRepository
          .findByComponentIdAndLanguage(id, lang)
          .orElseGet(() -> {
            ComponentTranslation nt = new ComponentTranslation();
            nt.setComponentId(id);
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

    var tr = translationRepository.findByComponentIdAndLanguage(saved.getId(), Language.TR).orElse(null);
    var en = translationRepository.findByComponentIdAndLanguage(saved.getId(), Language.EN).orElse(null);
    return ComponentMapper.toResponse(saved, tr, en);
  }

  @Override
  @Transactional
  public void delete(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));
    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }
    translationRepository.deleteByComponentId(component.getId());
    componentRepository.delete(component);
  }

  @Override
  public ComponentResponse get(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    var tr = translationRepository.findByComponentIdAndLanguage(id, Language.TR).orElse(null);
    var en = translationRepository.findByComponentIdAndLanguage(id, Language.EN).orElse(null);
    return ComponentMapper.toResponse(component, tr, en);
  }

  @Override
  public List<ComponentResponse> list(Long tenantId) {
    List<Component> components = componentRepository.findAllByTenantId(tenantId);
    if (components.isEmpty()) {
      return List.of();
    }

    List<Long> ids = components.stream().map(Component::getId).collect(Collectors.toList());
    List<ComponentTranslation> trList = translationRepository
        .findAllByComponentIdInAndLanguage(ids, Language.TR);
    List<ComponentTranslation> enList = translationRepository
        .findAllByComponentIdInAndLanguage(ids, Language.EN);

    var trByComp = trList.stream().collect(Collectors.toMap(ComponentTranslation::getComponentId, t -> t));
    var enByComp = enList.stream().collect(Collectors.toMap(ComponentTranslation::getComponentId, t -> t));

    return components.stream()
        .map(c -> ComponentMapper.toResponse(c, trByComp.get(c.getId()), enByComp.get(c.getId())))
        .collect(Collectors.toList());
  }

  @Override
  public List<ComponentResponse> list(Long tenantId, ComponentListFilter filter) {
    List<Component> components = (filter != null && filter.type() != null)
        ? componentRepository.findAllByTenantIdAndType(tenantId, filter.type())
        : componentRepository.findAllByTenantId(tenantId);

    if (components.isEmpty())
      return List.of();

    List<Long> ids = components.stream().map(Component::getId).collect(Collectors.toList());
    List<ComponentTranslation> trList = translationRepository
        .findAllByComponentIdInAndLanguage(ids, Language.TR);
    List<ComponentTranslation> enList = translationRepository
        .findAllByComponentIdInAndLanguage(ids, Language.EN);

    var trByComp = trList.stream().collect(Collectors.toMap(ComponentTranslation::getComponentId, t -> t));
    var enByComp = enList.stream().collect(Collectors.toMap(ComponentTranslation::getComponentId, t -> t));

    return components.stream()
        .map(c -> ComponentMapper.toResponse(c, trByComp.get(c.getId()), enByComp.get(c.getId())))
        .collect(Collectors.toList());
  }

  @Override
  public List<SiteComponentResponse> getSiteComponents(Long tenantId, ComponentType type, Language language) {
    List<Component> components = componentRepository.findActiveVisibleByTenantIdAndType(tenantId, type);

    if (components.isEmpty()) {
      return List.of();
    }
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .collect(Collectors.toList());
    List<ComponentTranslation> translations = translationRepository
        .findAllByComponentIdInAndLanguage(componentIds, language);
    Map<Long, ComponentTranslation> translationMap = translations.stream()
        .collect(Collectors.toMap(ComponentTranslation::getComponentId, t -> t));
    return components.stream()
        .map(component -> ComponentMapper.toSiteResponse(component, translationMap.get(component.getId())))
        .filter(response -> response != null) // Filter out null responses (no translation found)
        .collect(Collectors.toList());
  }

}