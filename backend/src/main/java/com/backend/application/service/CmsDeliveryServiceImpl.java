package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.dto.response.delivery.BatchDeliveryResponse;
import com.backend.presentation.dto.response.delivery.ComponentDeliveryResponse;
import com.backend.presentation.dto.response.delivery.EntryDeliveryResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CmsDeliveryServiceImpl implements CmsDeliveryService {

  private static final int MAX_BATCH_SIZE = 50;
  private static final Set<String> RESERVED_FIELDS = Set.of(
      "uid", "order", "title", "description", "isVisible", "styleClasses");

  private final ComponentRepository componentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentI18nRepository componentI18nRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final ComponentEntryI18nRepository componentEntryI18nRepository;
  private final TenantContext tenantContext;
  private final TenantPlatformRepository tenantPlatformRepository;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();

    Optional<Component> componentOpt = componentRepository.findByUid(uid);
    if (componentOpt.isEmpty()) {
      return Optional.empty();
    }

    Component component = componentOpt.get();
    if (component.getStatus() != ComponentStatus.PUBLISHED) {
      return Optional.empty();
    }

    return Optional.of(buildDeliveryResponse(component, resolvedLang));
  }

  @Override
  public BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();

    List<String> limitedUids = uids.size() > MAX_BATCH_SIZE
        ? uids.subList(0, MAX_BATCH_SIZE)
        : uids;

    Map<String, ComponentDeliveryResponse> data = new LinkedHashMap<>();
    List<String> notFound = new ArrayList<>();

    for (String uid : limitedUids) {
      Optional<ComponentDeliveryResponse> response = getComponentByUid(uid, resolvedLang);
      if (response.isPresent()) {
        data.put(uid, response.get());
      } else {
        notFound.add(uid);
      }
    }

    return BatchDeliveryResponse.builder()
        .data(data)
        .meta(BatchDeliveryResponse.BatchMeta.builder()
            .requested(limitedUids.size())
            .found(data.size())
            .notFound(notFound)
            .build())
        .build();
  }

  @Override
  public Language getDefaultLanguage() {
    String tenantIdStr = tenantContext.getTenantId();
    if (tenantIdStr == null) {
      return Language.TR;
    }

    Long tenantId = Long.parseLong(tenantIdStr);
    return tenantPlatformRepository.findById(tenantId)
        .map(Tenant::getDefaultLanguage)
        .map(Language::fromCode)
        .flatMap(opt -> opt)
        .orElse(Language.TR);
  }

  private ComponentDeliveryResponse buildDeliveryResponse(Component component, Language lang) {
    ComponentType componentType = componentTypeRepository.findById(component.getComponentTypeId())
        .orElse(null);

    Optional<ComponentI18n> i18nOpt = componentI18nRepository
        .findByComponentIdAndLanguage(component.getId(), lang);

    List<ComponentEntry> entries = componentEntryRepository
        .findByComponentIdAndStatusOrderBySortOrder(component.getId(), ComponentStatus.PUBLISHED);

    List<EntryDeliveryResponse> entryResponses = entries.stream()
        .map(entry -> buildEntryResponse(entry, lang))
        .toList();

    return ComponentDeliveryResponse.builder()
        .uid(component.getUid())
        .type(componentType != null ? componentType.getName() : null)
        .category(componentType != null ? componentType.getCategory() : null)
        .title(i18nOpt.map(ComponentI18n::getTitle).orElse(null))
        .subtitle(i18nOpt.map(ComponentI18n::getSubtitle).orElse(null))
        .description(i18nOpt.map(ComponentI18n::getDescription).orElse(null))
        .isVisible(component.getIsVisible())
        .styleClasses(component.getStyleClasses())
        .entries(entryResponses)
        .build();
  }

  private EntryDeliveryResponse buildEntryResponse(ComponentEntry entry, Language lang) {
    Optional<ComponentEntryI18n> i18nOpt = componentEntryI18nRepository
        .findByEntryIdAndLanguage(entry.getId(), lang);

    Map<String, Object> customFields = new HashMap<>();
    i18nOpt.ifPresent(i18n -> {
      if (i18n.getCustomData() != null) {
        customFields.putAll(parseCustomData(i18n.getCustomData()));
      }
    });

    customFields.keySet().removeAll(RESERVED_FIELDS);

    return EntryDeliveryResponse.builder()
        .uid(entry.getUid())
        .order(entry.getSortOrder())
        .title(i18nOpt.map(ComponentEntryI18n::getTitle).orElse(null))
        .description(i18nOpt.map(ComponentEntryI18n::getDescription).orElse(null))
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .customFields(customFields.isEmpty() ? null : customFields)
        .build();
  }

  private Map<String, Object> parseCustomData(String customDataJson) {
    if (customDataJson == null || customDataJson.isBlank()) {
      return new HashMap<>();
    }

    try {
      return objectMapper.readValue(customDataJson, new TypeReference<Map<String, Object>>() {
      });
    } catch (Exception e) {
      log.warn("Failed to parse custom_data JSON: {}", e.getMessage());
      return new HashMap<>();
    }
  }
}
