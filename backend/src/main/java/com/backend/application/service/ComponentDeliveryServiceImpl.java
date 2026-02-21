package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.util.MediaFieldExpander;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComponentDeliveryServiceImpl implements ComponentDeliveryService {

  private static final int MAX_BATCH_SIZE = 50;
  private static final Set<String> RESERVED_FIELDS = Set.of(
      "uid", "order", "title", "description", "isVisible", "styleClasses");

  private final ComponentRepository componentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentI18nRepository componentI18nRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final ComponentEntryI18nRepository componentEntryI18nRepository;
  private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
  private final ResponsiveMediaService responsiveMediaService;
  private final MediaFieldExpander mediaFieldExpander;
  private final NavigationService navigationService;

  @Override
  public Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang) {
    Optional<Component> componentOpt = componentRepository.findByUid(uid);
    if (componentOpt.isEmpty()) {
      return Optional.empty();
    }

    Component component = componentOpt.get();
    if (component.getStatus() != ComponentStatus.PUBLISHED) {
      return Optional.empty();
    }

    return Optional.of(buildDeliveryResponse(component, lang));
  }

  @Override
  public BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang) {
    List<String> limitedUids = uids.size() > MAX_BATCH_SIZE
        ? uids.subList(0, MAX_BATCH_SIZE)
        : uids;

    List<Component> components = componentRepository.findByUidInAndStatus(limitedUids, ComponentStatus.PUBLISHED);
    Map<String, Component> componentMap = components.stream()
        .collect(Collectors.toMap(Component::getUid, c -> c));
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .toList();
    Map<Long, ComponentI18n> componentI18nMap = componentI18nRepository
        .findByComponentIdInAndLanguage(componentIds, lang)
        .stream()
        .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i));
    List<ComponentEntry> allEntries = componentEntryRepository
        .findByComponentIdInAndStatusOrderBySortOrder(componentIds, ComponentStatus.PUBLISHED);
    Map<Long, List<ComponentEntry>> entriesByComponentId = allEntries.stream()
        .collect(Collectors.groupingBy(ComponentEntry::getComponentId));
    List<Long> entryIds = allEntries.stream()
        .map(ComponentEntry::getId)
        .toList();

    Map<Long, ComponentEntryI18n> entryI18nMap = componentEntryI18nRepository
        .findByEntryIdInAndLanguage(entryIds, lang)
        .stream()
        .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i));

    List<Long> typeIds = components.stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    Map<Long, ComponentType> typeMap = typeIds.isEmpty()
        ? Map.of()
        : componentTypeRepository.findByIdIn(typeIds).stream()
            .collect(Collectors.toMap(ComponentType::getId, t -> t));

    // Batch fetch responsive media sets with eagerly loaded media and translations
    List<Long> responsiveMediaIds = components.stream()
        .map(Component::getResponsiveMedia)
        .filter(r -> r != null)
        .map(ResponsiveMediaSet::getId)
        .distinct()
        .toList();
    Map<Long, ResponsiveMediaSet> responsiveMediaMap = responsiveMediaIds.isEmpty()
        ? Map.of()
        : responsiveMediaSetRepository.findByIdInWithMedia(responsiveMediaIds).stream()
            .collect(Collectors.toMap(ResponsiveMediaSet::getId, r -> r));

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
          .map(entry -> buildEntryResponseOptimized(entry, entryI18nMap.get(entry.getId()),
              component.getComponentTypeId(), lang))
          .toList();

      // Build responsive media for component using the pre-fetched map
      ResponsiveMediaDeliveryResponse responsive = null;
      ResponsiveMediaSet responsiveMedia = component.getResponsiveMedia() != null
          ? responsiveMediaMap.get(component.getResponsiveMedia().getId())
          : null;
      if (responsiveMedia != null) {
        responsive = responsiveMediaService.toDeliveryResponse(responsiveMedia, lang);
      }

      ComponentDeliveryResponse response = ComponentDeliveryResponse.builder()
          .uid(component.getUid())
          .type(resolveComponentType(type))
          .category(type != null ? type.getCategory() : null)
          .title(i18n != null ? i18n.getTitle() : null)
          .subtitle(i18n != null ? i18n.getSubtitle() : null)
          .description(i18n != null ? i18n.getDescription() : null)
          .isVisible(component.getIsVisible())
          .styleClasses(component.getStyleClasses())
          .navigationType(resolveNavigationType(type, component))
          .searchBox(resolveSearchBox(type, component))
          .navigationNode(resolveNavigationNode(type, component))
          .navigationLinkNode(resolveNavigationLinkNode(type, component))
          .responsive(responsive)
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

  EntryDeliveryResponse buildEntryResponseOptimized(ComponentEntry entry, ComponentEntryI18n i18n,
      Long componentTypeId, Language lang) {
    Map<String, Object> customFields = new HashMap<>();
    if (i18n != null && i18n.getCustomData() != null) {
      customFields.putAll(mediaFieldExpander.parseCustomData(i18n.getCustomData()));
    }
    customFields.keySet().removeAll(RESERVED_FIELDS);

    // Expand MEDIA fields to full responsive media objects
    if (!customFields.isEmpty() && componentTypeId != null) {
      customFields = mediaFieldExpander.expandMediaFields(customFields, componentTypeId, lang);
    }

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

  private ComponentDeliveryResponse buildDeliveryResponse(Component component, Language lang) {
    ComponentType componentType = componentTypeRepository.findById(component.getComponentTypeId())
        .orElse(null);

    Optional<ComponentI18n> i18nOpt = componentI18nRepository
        .findByComponentIdAndLanguage(component.getId(), lang);

    List<ComponentEntry> entries = componentEntryRepository
        .findByComponentIdAndStatusOrderBySortOrder(component.getId(), ComponentStatus.PUBLISHED);

    List<EntryDeliveryResponse> entryResponses = entries.stream()
        .map(entry -> buildEntryResponse(entry, component.getComponentTypeId(), lang))
        .toList();

    // Build responsive media for component
    ResponsiveMediaDeliveryResponse responsive = null;
    if (component.getResponsiveMedia() != null) {
      responsive = responsiveMediaService.toDeliveryResponse(component.getResponsiveMedia(), lang);
    }

    return ComponentDeliveryResponse.builder()
        .uid(component.getUid())
        .type(resolveComponentType(componentType))
        .category(componentType != null ? componentType.getCategory() : null)
        .title(i18nOpt.map(ComponentI18n::getTitle).orElse(null))
        .subtitle(i18nOpt.map(ComponentI18n::getSubtitle).orElse(null))
        .description(i18nOpt.map(ComponentI18n::getDescription).orElse(null))
        .isVisible(component.getIsVisible())
        .styleClasses(component.getStyleClasses())
        .navigationType(resolveNavigationType(componentType, component))
        .searchBox(resolveSearchBox(componentType, component))
        .navigationNode(resolveNavigationNode(componentType, component))
        .navigationLinkNode(resolveNavigationLinkNode(componentType, component))
        .responsive(responsive)
        .entries(entryResponses)
        .build();
  }

  private EntryDeliveryResponse buildEntryResponse(ComponentEntry entry, Long componentTypeId, Language lang) {
    Optional<ComponentEntryI18n> i18nOpt = componentEntryI18nRepository
        .findByEntryIdAndLanguage(entry.getId(), lang);

    Map<String, Object> customFields = new HashMap<>();
    i18nOpt.ifPresent(i18n -> {
      if (i18n.getCustomData() != null) {
        customFields.putAll(mediaFieldExpander.parseCustomData(i18n.getCustomData()));
      }
    });

    customFields.keySet().removeAll(RESERVED_FIELDS);

    // Expand MEDIA fields to full responsive media objects
    Map<String, Object> expandedFields = customFields;
    if (!customFields.isEmpty() && componentTypeId != null) {
      expandedFields = mediaFieldExpander.expandMediaFields(customFields, componentTypeId, lang);
    }

    return EntryDeliveryResponse.builder()
        .uid(entry.getUid())
        .order(entry.getSortOrder())
        .title(i18nOpt.map(ComponentEntryI18n::getTitle).orElse(null))
        .description(i18nOpt.map(ComponentEntryI18n::getDescription).orElse(null))
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .customFields(expandedFields.isEmpty() ? null : expandedFields)
        .build();
  }

  private String resolveComponentType(ComponentType type) {
    if (type == null) {
      return null;
    }
    return type.getUid() != null ? type.getUid() : type.getName();
  }

  private NavigationType resolveNavigationType(ComponentType type, Component component) {
    if (!isNavigationAware(type)) {
      return null;
    }
    return component.getNavigationType();
  }

  private Boolean resolveSearchBox(ComponentType type, Component component) {
    if (!isNavigationAware(type)) {
      return null;
    }
    return component.getSearchBox();
  }

  private NavigationDeliveryResponse resolveNavigationNode(ComponentType type, Component component) {
    if (!isNavigationAware(type) || component.getNavigationNodeId() == null) {
      return null;
    }
    return navigationService.getNavigationById(component.getNavigationNodeId()).orElse(null);
  }

  private NavigationDeliveryResponse resolveNavigationLinkNode(ComponentType type, Component component) {
    // navigationLinkNodeId is reserved for future use and hidden from UI
    return null;
  }

  private boolean isNavigationAware(ComponentType type) {
    return type != null && type.isNavigationAware();
  }
}
