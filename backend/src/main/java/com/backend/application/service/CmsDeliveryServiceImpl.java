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

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.TenantRepository;
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
  private final TenantContextPort tenantContext;
  private final TenantRepository tenantRepository;
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

    List<Component> components = componentRepository.findByUidInAndStatus(limitedUids, ComponentStatus.PUBLISHED);
    Map<String, Component> componentMap = components.stream()
        .collect(java.util.stream.Collectors.toMap(Component::getUid, c -> c));
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .toList();
    Map<Long, ComponentI18n> componentI18nMap = componentI18nRepository
        .findByComponentIdInAndLanguage(componentIds, resolvedLang)
        .stream()
        .collect(java.util.stream.Collectors.toMap(ComponentI18n::getComponentId, i -> i));
    List<ComponentEntry> allEntries = componentEntryRepository
        .findByComponentIdInAndStatusOrderBySortOrder(componentIds, ComponentStatus.PUBLISHED);
    Map<Long, List<ComponentEntry>> entriesByComponentId = allEntries.stream()
        .collect(java.util.stream.Collectors.groupingBy(ComponentEntry::getComponentId));
    List<Long> entryIds = allEntries.stream()
        .map(ComponentEntry::getId)
        .toList();

    Map<Long, ComponentEntryI18n> entryI18nMap = componentEntryI18nRepository
        .findByEntryIdInAndLanguage(entryIds, resolvedLang)
        .stream()
        .collect(java.util.stream.Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i));

    java.util.Set<Long> typeIds = components.stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .collect(java.util.stream.Collectors.toSet());
    Map<Long, ComponentType> typeMap = typeIds.stream()
        .map(componentTypeRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(java.util.stream.Collectors.toMap(ComponentType::getId, t -> t));

    Map<String, ComponentDeliveryResponse> data = new LinkedHashMap<>();
    List<String> notFound = new ArrayList<>();

    for (String uid : limitedUids) {
      Component component = componentMap.get(uid);
      if (component == null) {
        notFound.add(uid);
        continue;
      }

      ComponentI18n i18n = componentI18nMap.get(component.getId());
      ComponentType type = component.getComponentTypeId() != null
          ? typeMap.get(component.getComponentTypeId())
          : null;
      List<ComponentEntry> entries = entriesByComponentId.getOrDefault(component.getId(), List.of());

      List<EntryDeliveryResponse> entryResponses = entries.stream()
          .map(entry -> buildEntryResponseOptimized(entry, entryI18nMap.get(entry.getId())))
          .toList();

      ComponentDeliveryResponse response = ComponentDeliveryResponse.builder()
          .uid(component.getUid())
          .type(type != null ? type.getName() : null)
          .category(type != null ? type.getCategory() : null)
          .title(i18n != null ? i18n.getTitle() : null)
          .subtitle(i18n != null ? i18n.getSubtitle() : null)
          .description(i18n != null ? i18n.getDescription() : null)
          .isVisible(component.getIsVisible())
          .styleClasses(component.getStyleClasses())
          .entries(entryResponses)
          .build();

      data.put(uid, response);
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

  /**
   * Optimized entry response builder using pre-fetched i18n data.
   */
  private EntryDeliveryResponse buildEntryResponseOptimized(ComponentEntry entry, ComponentEntryI18n i18n) {
    Map<String, Object> customFields = new HashMap<>();
    if (i18n != null && i18n.getCustomData() != null) {
      customFields.putAll(parseCustomData(i18n.getCustomData()));
    }
    customFields.keySet().removeAll(RESERVED_FIELDS);

    return EntryDeliveryResponse.builder()
        .uid(entry.getUid())
        .order(entry.getSortOrder())
        .title(i18n != null ? i18n.getTitle() : null)
        .description(i18n != null ? i18n.getDescription() : null)
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .customFields(customFields.isEmpty() ? null : customFields)
        .build();
  }

  @Override
  public Language getDefaultLanguage() {
    String tenantIdStr = tenantContext.getTenantId();
    if (tenantIdStr == null) {
      return Language.TR;
    }

    try {
      Long tenantId = Long.parseLong(tenantIdStr);
      return tenantRepository.findById(tenantId)
          .map(Tenant::getDefaultLanguage)
          .orElse(Language.TR);
    } catch (NumberFormatException e) {
      log.warn("Invalid tenant ID format: {}", tenantIdStr);
      return Language.TR;
    }
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
