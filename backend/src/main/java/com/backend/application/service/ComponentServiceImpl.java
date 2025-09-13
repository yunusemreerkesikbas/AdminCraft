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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComponentServiceImpl implements ComponentService {

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private ComponentTranslationRepository translationRepository;

  @Override
  @Transactional
  public ComponentResponse create(CreateComponentRequest request, Long createdBy) {
    componentRepository.findByTenantAndTypeAndKey(request.tenantId(), request.type(), request.key())
        .ifPresent(c -> {
          throw new ComponentConflictException("ui.component.key.conflict");
        });

    Component component = new Component();
    component.setTenantId(request.tenantId());
    component.setType(request.type());
    component.setKey(request.key());
    component.setStatus(request.status() != null ? request.status() : component.getStatus());
    component.setVisible(Boolean.TRUE.equals(request.visible()));
    component.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
    component.setCreatedBy(createdBy);

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
  public ComponentResponse update(Long id, Long tenantId, UpdateComponentRequest request, Long updatedBy) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));

    if (request.status() != null)
      component.setStatus(request.status());
    if (request.visible() != null)
      component.setVisible(request.visible());
    if (request.sortOrder() != null)
      component.setSortOrder(request.sortOrder());
    component.setUpdatedBy(updatedBy);

    var saved = componentRepository.save(component);

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
    translationRepository.deleteByComponentId(component.getId());
    componentRepository.delete(component);
  }

  @Override
  public ComponentResponse get(Long id, Long tenantId) {
    Component component = componentRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ComponentNotFoundException("ui.component.not.found"));
    var tr = translationRepository.findByComponentIdAndLanguage(id, Language.TR).orElse(null);
    var en = translationRepository.findByComponentIdAndLanguage(id, Language.EN).orElse(null);
    return ComponentMapper.toResponse(component, tr, en);
  }

  @Override
  public List<ComponentResponse> list(Long tenantId) {
    return componentRepository.findAllByTenantId(tenantId)
        .stream()
        .map(c -> {
          var tr = translationRepository.findByComponentIdAndLanguage(c.getId(), Language.TR).orElse(null);
          var en = translationRepository.findByComponentIdAndLanguage(c.getId(), Language.EN).orElse(null);
          return ComponentMapper.toResponse(c, tr, en);
        })
        .collect(Collectors.toList());
  }
}
