package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.exception.ComponentConflictException;
import com.backend.domain.exception.ComponentNotFoundException;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.presentation.dto.request.CreateComponentRequest;
import com.backend.presentation.dto.request.UpdateComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
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

  // Constructor injection following Clean Architecture principles
  public ComponentServiceImpl(ComponentRepository componentRepository,
      ComponentTranslationRepository translationRepository) {
    this.componentRepository = componentRepository;
    this.translationRepository = translationRepository;
  }

  @Override
  @Transactional
  public ComponentResponse create(Long tenantId, CreateComponentRequest request) {
    componentRepository.findByTenantAndTypeAndKey(tenantId, request.type(), request.key())
        .ifPresent(c -> {
          throw new ComponentConflictException("ui.component.key.conflict");
        });

    // Create component using domain logic for defaults
    Component component = new Component();
    component.setTenantId(tenantId);
    component.setType(request.type());
    component.setKey(request.key());
    component.setStatus(request.status() != null ? request.status() : component.getStatus());
    component.setVisible(Boolean.TRUE.equals(request.visible()));
    component.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
    component.setCreatedBy(com.backend.shared.common.SecurityUtil.getCurrentUserIdOrThrow());

    var saved = componentRepository.save(component);

    // TR translation
    ComponentTranslation tr = new ComponentTranslation();
    tr.setComponentId(saved.getId());
    tr.setLanguage(Language.TR);
    tr.setTitle(request.titleTr());
    tr.setSubtitle(request.subtitleTr());
    tr.setData(request.dataTr());
    translationRepository.save(tr);

    // EN translation
    ComponentTranslation en = new ComponentTranslation();
    en.setComponentId(saved.getId());
    en.setLanguage(Language.EN);
    en.setTitle(request.titleEn());
    en.setSubtitle(request.subtitleEn());
    en.setData(request.dataEn());
    translationRepository.save(en);

    return ComponentMapper.toResponse(saved, tr, en);
  }

  @Override
  @Transactional
  public ComponentResponse update(Long id, Long tenantId, UpdateComponentRequest request) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    // Validate tenant access using domain method
    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    // Use domain methods for updates
    if (request.status() != null) {
      component.setStatus(request.status());
    }
    if (request.visible() != null)
      component.setVisible(request.visible());
    if (request.sortOrder() != null)
      component.setSortOrder(request.sortOrder());
    component.setUpdatedBy(com.backend.shared.common.SecurityUtil.getCurrentUserIdOrThrow());

    var saved = componentRepository.save(component);

    // Update TR translation
    ComponentTranslation tr = translationRepository
        .findByComponentIdAndLanguage(id, Language.TR)
        .orElseGet(() -> {
          ComponentTranslation t = new ComponentTranslation();
          t.setComponentId(id);
          t.setLanguage(Language.TR);
          return t;
        });
    if (request.titleTr() != null)
      tr.setTitle(request.titleTr());
    if (request.subtitleTr() != null)
      tr.setSubtitle(request.subtitleTr());
    if (request.dataTr() != null)
      tr.setData(request.dataTr());
    translationRepository.save(tr);

    // Update EN translation
    ComponentTranslation en = translationRepository
        .findByComponentIdAndLanguage(id, Language.EN)
        .orElseGet(() -> {
          ComponentTranslation t = new ComponentTranslation();
          t.setComponentId(id);
          t.setLanguage(Language.EN);
          return t;
        });
    if (request.titleEn() != null)
      en.setTitle(request.titleEn());
    if (request.subtitleEn() != null)
      en.setSubtitle(request.subtitleEn());
    if (request.dataEn() != null)
      en.setData(request.dataEn());
    translationRepository.save(en);

    return ComponentMapper.toResponse(saved, tr, en);
  }

  @Override
  @Transactional
  public void delete(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    // Validate tenant access using domain method
    if (!component.isValidForTenant(tenantId)) {
      throw new ComponentNotFoundException("ui.component.not.found");
    }

    // Delete translations first, then component
    translationRepository.deleteByComponentId(component.getId());
    componentRepository.delete(component);
  }

  @Override
  public ComponentResponse get(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    // Validate tenant access using domain method
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
}