package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.cms.preview.CmsRequestContext;
import com.backend.application.cms.preview.CmsDraftOverrideService;
import com.backend.application.cms.preview.CmsVisibility;
import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.util.MediaFieldExpander;
import com.backend.application.util.UrlUtils;
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
@Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
public class ComponentDeliveryServiceImpl implements ComponentDeliveryService {

  private static final int MAX_BATCH_SIZE = 50;
  private static final Set<String> RESERVED_FIELDS = Set.of(
      "uid", "order", "title", "description", "isVisible", "styleClasses", "isExternal");

  private final ComponentRepository componentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentI18nRepository componentI18nRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final ComponentEntryI18nRepository componentEntryI18nRepository;
  private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
  private final ResponsiveMediaService responsiveMediaService;
  private final MediaFieldExpander mediaFieldExpander;
  private final NavigationService navigationService;
  private final CmsRequestContext cmsRequestContext;
  private final CmsDraftOverrideService cmsDraftOverrideService;

  @Override
  public Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang) {
    Optional<Component> componentOpt = componentRepository.findByUid(uid);
    if (componentOpt.isEmpty()) {
      return Optional.empty();
    }

    Component component = cloneComponent(componentOpt.get());
    if (!CmsVisibility.componentStatuses(cmsRequestContext.isPreview()).contains(component.getStatus())) {
      return Optional.empty();
    }
    if (cmsRequestContext.isPreview()) {
      cmsDraftOverrideService.findComponentDraft(component.getId())
          .ifPresent(draft -> cmsDraftOverrideService.apply(component, draft));
    }

    return Optional.of(buildDeliveryResponse(component, lang));
  }

  @Override
  public BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang) {
    List<String> limitedUids = uids.size() > MAX_BATCH_SIZE
        ? uids.subList(0, MAX_BATCH_SIZE)
        : uids;

    Set<ComponentStatus> visibleStatuses = CmsVisibility.componentStatuses(cmsRequestContext.isPreview());
    List<Component> components = componentRepository.findByUidInAndStatusIn(limitedUids, visibleStatuses);
    Map<String, Component> componentMap = components.stream()
        .map(this::cloneComponent)
        .collect(Collectors.toMap(Component::getUid, c -> c));
    if (cmsRequestContext.isPreview() && !componentMap.isEmpty()) {
      Map<Long, com.backend.application.cms.preview.ComponentDraftPayload> drafts =
          cmsDraftOverrideService.findComponentDrafts(componentMap.values().stream().map(Component::getId).toList());
      componentMap.values().forEach(component ->
          cmsDraftOverrideService.apply(component, drafts.get(component.getId())));
    }
    List<Long> componentIds = components.stream()
        .map(Component::getId)
        .toList();
    Map<Long, ComponentI18n> componentI18nMap = new LinkedHashMap<>(
        componentI18nRepository
            .findByComponentIdInAndLanguage(componentIds, lang)
            .stream()
            .map(this::cloneComponentI18n)
            .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i)));
    if (cmsRequestContext.isPreview() && !componentIds.isEmpty()) {
      Map<String, com.backend.application.cms.preview.ComponentI18nDraftPayload> i18nDrafts =
          cmsDraftOverrideService.findComponentI18nDrafts(componentIds, lang);
      for (Long componentId : componentIds) {
        var draft = i18nDrafts.get(cmsDraftOverrideService.i18nKey(componentId, lang));
        if (draft == null) {
          continue;
        }
        ComponentI18n i18n = componentI18nMap.get(componentId);
        if (i18n == null) {
          i18n = new ComponentI18n();
          i18n.setComponentId(componentId);
          i18n.setLanguage(lang);
          componentI18nMap.put(componentId, i18n);
        }
        cmsDraftOverrideService.apply(i18n, draft);
      }
    }
    List<ComponentEntry> allEntries = componentEntryRepository
        .findByComponentIdInAndStatusInOrderBySortOrder(componentIds, visibleStatuses)
        .stream()
        .map(this::cloneComponentEntry)
        .toList();
    Map<Long, List<ComponentEntry>> entriesByComponentId = groupEntriesByComponentId(allEntries);
    List<Long> entryIds = allEntries.stream()
        .map(ComponentEntry::getId)
        .toList();

    if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
      Map<Long, com.backend.application.cms.preview.ComponentEntryDraftPayload> entryDrafts =
          cmsDraftOverrideService.findComponentEntryDrafts(entryIds);
      allEntries.forEach(entry -> cmsDraftOverrideService.apply(entry, entryDrafts.get(entry.getId())));
      entriesByComponentId = groupEntriesByComponentId(allEntries);
    }

    Map<Long, ComponentEntryI18n> entryI18nMap = new LinkedHashMap<>(componentEntryI18nRepository
        .findByEntryIdInAndLanguage(entryIds, lang)
        .stream()
        .map(this::cloneComponentEntryI18n)
        .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i)));
    if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
      Map<String, com.backend.application.cms.preview.ComponentEntryI18nDraftPayload> i18nDrafts =
          cmsDraftOverrideService.findComponentEntryI18nDrafts(entryIds, lang);
      for (Long entryId : entryIds) {
        var draft = i18nDrafts.get(cmsDraftOverrideService.i18nKey(entryId, lang));
        if (draft == null) {
          continue;
        }
        ComponentEntryI18n i18n = entryI18nMap.get(entryId);
        if (i18n == null) {
          i18n = new ComponentEntryI18n();
          i18n.setEntryId(entryId);
          i18n.setLanguage(lang);
          entryI18nMap.put(entryId, i18n);
        }
        cmsDraftOverrideService.apply(i18n, draft);
      }
    }

    List<Long> typeIds = componentMap.values().stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    Map<Long, ComponentType> typeMap = typeIds.isEmpty()
        ? Map.of()
        : componentTypeRepository.findByIdIn(typeIds).stream()
            .collect(Collectors.toMap(ComponentType::getId, t -> t));

    // Batch fetch responsive media sets with eagerly loaded media and translations
    List<Long> responsiveMediaIds = componentMap.values().stream()
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

      ComponentDeliveryResponse response = new ComponentDeliveryResponse(
          component.getUid(),
          resolveComponentType(type),
          type != null ? type.getCategory() : null,
          i18n != null ? i18n.getTitle() : null,
          i18n != null ? i18n.getSubtitle() : null,
          i18n != null ? i18n.getDescription() : null,
          component.getIsVisible(),
          component.getStyleClasses(),
          buildComponentCustomFields(component, component.getComponentTypeId(), lang),
          resolveNavigationType(type, component),
          resolveSearchBox(type, component),
          resolveNavigationNode(type, component, lang),
          responsive,
          entryResponses);

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

    ResponsiveMediaDeliveryResponse responsive = null;
    if (entry.getResponsiveMedia() != null) {
      responsive = responsiveMediaService.toDeliveryResponse(entry.getResponsiveMedia(), lang);
    }

    return EntryDeliveryResponse.builder()
        .uid(entry.getUid())
        .order(entry.getSortOrder())
        .title(i18n != null ? i18n.getTitle() : null)
        .description(i18n != null ? i18n.getDescription() : null)
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .isExternal(UrlUtils.computeIsExternal(customFields))
        .responsive(responsive)
        .customFields(customFields.isEmpty() ? null : customFields)
        .build();
  }

  private ComponentDeliveryResponse buildDeliveryResponse(Component component, Language lang) {
    ComponentType componentType = componentTypeRepository.findById(component.getComponentTypeId())
        .orElse(null);

    Optional<ComponentI18n> i18nOpt = componentI18nRepository
        .findByComponentIdAndLanguage(component.getId(), lang)
        .map(this::cloneComponentI18n);
    if (cmsRequestContext.isPreview()) {
      ComponentI18n i18n = i18nOpt.orElseGet(() -> {
        ComponentI18n created = new ComponentI18n();
        created.setComponentId(component.getId());
        created.setLanguage(lang);
        return created;
      });
      cmsDraftOverrideService.findComponentI18nDraft(component.getId(), lang)
          .ifPresent(draft -> cmsDraftOverrideService.apply(i18n, draft));
      i18nOpt = Optional.of(i18n);
    }

    Set<ComponentStatus> visibleStatuses = CmsVisibility.componentStatuses(cmsRequestContext.isPreview());
    List<ComponentEntry> entries = componentEntryRepository
        .findByComponentIdInAndStatusInOrderBySortOrder(List.of(component.getId()), visibleStatuses)
        .stream()
        .map(this::cloneComponentEntry)
        .toList();
    List<Long> entryIds = entries.stream()
        .map(ComponentEntry::getId)
        .toList();
    if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
      Map<Long, com.backend.application.cms.preview.ComponentEntryDraftPayload> entryDrafts =
          cmsDraftOverrideService.findComponentEntryDrafts(entryIds);
      entries.forEach(entry -> cmsDraftOverrideService.apply(entry, entryDrafts.get(entry.getId())));
      entries = sortEntries(entries);
    }
    Map<Long, ComponentEntryI18n> entryI18nMap = entryIds.isEmpty()
        ? Map.of()
        : new LinkedHashMap<>(componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds, lang)
            .stream()
            .map(this::cloneComponentEntryI18n)
            .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i)));
    if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
      Map<String, com.backend.application.cms.preview.ComponentEntryI18nDraftPayload> i18nDrafts =
          cmsDraftOverrideService.findComponentEntryI18nDrafts(entryIds, lang);
      for (Long entryId : entryIds) {
        var draft = i18nDrafts.get(cmsDraftOverrideService.i18nKey(entryId, lang));
        if (draft == null) {
          continue;
        }
        ComponentEntryI18n i18n = entryI18nMap.get(entryId);
        if (i18n == null) {
          i18n = new ComponentEntryI18n();
          i18n.setEntryId(entryId);
          i18n.setLanguage(lang);
          entryI18nMap.put(entryId, i18n);
        }
        cmsDraftOverrideService.apply(i18n, draft);
      }
    }

    List<EntryDeliveryResponse> entryResponses = entries.stream()
        .map(entry -> buildEntryResponseOptimized(entry, entryI18nMap.get(entry.getId()), component.getComponentTypeId(), lang))
        .toList();

    // Build responsive media for component
    ResponsiveMediaDeliveryResponse responsive = null;
    if (component.getResponsiveMedia() != null) {
      responsive = responsiveMediaService.toDeliveryResponse(component.getResponsiveMedia(), lang);
    }

    return new ComponentDeliveryResponse(
        component.getUid(),
        resolveComponentType(componentType),
        componentType != null ? componentType.getCategory() : null,
        i18nOpt.map(ComponentI18n::getTitle).orElse(null),
        i18nOpt.map(ComponentI18n::getSubtitle).orElse(null),
        i18nOpt.map(ComponentI18n::getDescription).orElse(null),
        component.getIsVisible(),
        component.getStyleClasses(),
        buildComponentCustomFields(component, component.getComponentTypeId(), lang),
        resolveNavigationType(componentType, component),
        resolveSearchBox(componentType, component),
        resolveNavigationNode(componentType, component, lang),
        responsive,
        entryResponses);
  }

  private EntryDeliveryResponse buildEntryResponse(ComponentEntry entry, Long componentTypeId, Language lang) {
    if (cmsRequestContext.isPreview()) {
      cmsDraftOverrideService.findComponentEntryDrafts(List.of(entry.getId()))
          .values()
          .stream()
          .findFirst()
          .ifPresent(draft -> cmsDraftOverrideService.apply(entry, draft));
    }
    Optional<ComponentEntryI18n> i18nOpt = componentEntryI18nRepository
        .findByEntryIdAndLanguage(entry.getId(), lang)
        .map(this::cloneComponentEntryI18n);
    if (cmsRequestContext.isPreview()) {
      ComponentEntryI18n i18n = i18nOpt.orElseGet(() -> {
        ComponentEntryI18n created = new ComponentEntryI18n();
        created.setEntryId(entry.getId());
        created.setLanguage(lang);
        return created;
      });
      cmsDraftOverrideService.findComponentEntryI18nDrafts(List.of(entry.getId()), lang)
          .values()
          .stream()
          .findFirst()
          .ifPresent(draft -> cmsDraftOverrideService.apply(i18n, draft));
      i18nOpt = Optional.of(i18n);
    }

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

    ResponsiveMediaDeliveryResponse responsive = null;
    if (entry.getResponsiveMedia() != null) {
      responsive = responsiveMediaService.toDeliveryResponse(entry.getResponsiveMedia(), lang);
    }

    return EntryDeliveryResponse.builder()
        .uid(entry.getUid())
        .order(entry.getSortOrder())
        .title(i18nOpt.map(ComponentEntryI18n::getTitle).orElse(null))
        .description(i18nOpt.map(ComponentEntryI18n::getDescription).orElse(null))
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .isExternal(UrlUtils.computeIsExternal(expandedFields))
        .responsive(responsive)
        .customFields(expandedFields.isEmpty() ? null : expandedFields)
        .build();
  }

  private String resolveComponentType(ComponentType type) {
    if (type == null) {
      return null;
    }
    return type.getUid() != null ? type.getUid() : type.getName();
  }

  private Map<Long, List<ComponentEntry>> groupEntriesByComponentId(List<ComponentEntry> entries) {
    return sortEntries(entries).stream()
        .collect(Collectors.groupingBy(ComponentEntry::getComponentId, LinkedHashMap::new, Collectors.toList()));
  }

  private List<ComponentEntry> sortEntries(List<ComponentEntry> entries) {
    return entries.stream()
        .sorted(Comparator.comparingInt(entry -> entry.getSortOrder() != null ? entry.getSortOrder() : 0))
        .toList();
  }

  private Map<String, Object> buildComponentCustomFields(Component component, Long componentTypeId, Language lang) {
    Map<String, Object> customFields = new LinkedHashMap<>();
    if (component.getCustomData() != null && !component.getCustomData().isBlank()) {
      customFields.putAll(mediaFieldExpander.parseCustomData(component.getCustomData()));
    }

    if (!customFields.isEmpty() && componentTypeId != null) {
      customFields = mediaFieldExpander.expandMediaFields(customFields, componentTypeId, lang);
    }

    return customFields.isEmpty() ? null : customFields;
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

  private NavigationDeliveryResponse resolveNavigationNode(ComponentType type, Component component, Language lang) {
    if (!isNavigationAware(type) || component.getNavigationNodeId() == null) {
      return null;
    }
    return navigationService.getNavigationById(component.getNavigationNodeId(), lang).orElse(null);
  }

  private boolean isNavigationAware(ComponentType type) {
    return type != null && type.isNavigationAware();
  }

  private Component cloneComponent(Component source) {
    Component copy = new Component();
    copy.setId(source.getId());
    copy.setUuid(source.getUuid());
    copy.setUid(source.getUid());
    copy.setComponentTypeId(source.getComponentTypeId());
    copy.setName(source.getName());
    copy.setDisplayOrder(source.getDisplayOrder());
    copy.setIsVisible(source.getIsVisible());
    copy.setStyleClasses(source.getStyleClasses());
    copy.setCustomData(source.getCustomData());
    copy.setStatus(source.getStatus());
    copy.setResponsiveMedia(source.getResponsiveMedia());
    copy.setNavigationNodeId(source.getNavigationNodeId());
    copy.setNavigationType(source.getNavigationType());
    copy.setSearchBox(source.getSearchBox());
    return copy;
  }

  private ComponentI18n cloneComponentI18n(ComponentI18n source) {
    ComponentI18n copy = new ComponentI18n();
    copy.setId(source.getId());
    copy.setUuid(source.getUuid());
    copy.setUid(source.getUid());
    copy.setComponentId(source.getComponentId());
    copy.setLanguage(source.getLanguage());
    copy.setTitle(source.getTitle());
    copy.setSubtitle(source.getSubtitle());
    copy.setDescription(source.getDescription());
    copy.setStatus(source.getStatus());
    return copy;
  }

  private ComponentEntry cloneComponentEntry(ComponentEntry source) {
    ComponentEntry copy = new ComponentEntry();
    copy.setId(source.getId());
    copy.setUuid(source.getUuid());
    copy.setUid(source.getUid());
    copy.setComponentId(source.getComponentId());
    copy.setSortOrder(source.getSortOrder());
    copy.setIsVisible(source.getIsVisible());
    copy.setStyleClasses(source.getStyleClasses());
    copy.setStatus(source.getStatus());
    copy.setResponsiveMedia(source.getResponsiveMedia());
    return copy;
  }

  private ComponentEntryI18n cloneComponentEntryI18n(ComponentEntryI18n source) {
    ComponentEntryI18n copy = new ComponentEntryI18n();
    copy.setId(source.getId());
    copy.setUuid(source.getUuid());
    copy.setUid(source.getUid());
    copy.setEntryId(source.getEntryId());
    copy.setLanguage(source.getLanguage());
    copy.setTitle(source.getTitle());
    copy.setDescription(source.getDescription());
    copy.setCustomData(source.getCustomData());
    copy.setStatus(source.getStatus());
    return copy;
  }

}
