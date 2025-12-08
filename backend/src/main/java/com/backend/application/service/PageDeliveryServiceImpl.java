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

import com.backend.application.dto.delivery.BatchPageDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PageDeliveryServiceImpl implements PageDeliveryService {

  private static final int MAX_BATCH_SIZE = 50;
  private static final Set<String> RESERVED_FIELDS = Set.of(
      "uid", "order", "title", "description", "isVisible", "styleClasses");

  private final PageRepository pageRepository;
  private final PageI18nRepository pageI18nRepository;
  private final PageSlotRepository pageSlotRepository;
  private final SlotComponentRepository slotComponentRepository;
  private final ComponentRepository componentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentI18nRepository componentI18nRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final ComponentEntryI18nRepository componentEntryI18nRepository;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<PageDeliveryResponse> getPageByUid(String uid, Language lang) {
    Optional<Page> pageOpt = pageRepository.findByUid(uid);
    if (pageOpt.isEmpty()) {
      return Optional.empty();
    }

    Page page = pageOpt.get();
    return Optional.of(buildPageDeliveryResponse(page, lang));
  }

  @Override
  public BatchPageDeliveryResponse getPagesByUids(List<String> uids, Language lang) {
    List<String> limitedUids = uids.size() > MAX_BATCH_SIZE
        ? uids.subList(0, MAX_BATCH_SIZE)
        : uids;

    List<Page> pages = pageRepository.findByUidIn(limitedUids);
    Map<String, Page> pageMap = pages.stream()
        .collect(Collectors.toMap(Page::getUid, p -> p));

    List<Long> pageIds = pages.stream().map(Page::getId).toList();

    Map<Long, PageI18n> pageI18nMap = pageI18nRepository
        .findByPageIdInAndLanguage(pageIds, lang)
        .stream()
        .collect(Collectors.toMap(PageI18n::getPageId, i -> i));

    List<PageSlot> allSlots = new ArrayList<>();
    for (Long pageId : pageIds) {
      allSlots.addAll(pageSlotRepository.findByPageId(pageId));
    }
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();
    allSlots.addAll(sharedSlots);

    List<Long> slotIds = allSlots.stream().map(PageSlot::getId).distinct().toList();

    List<SlotComponent> allSlotComponents = slotComponentRepository.findBySlotIdIn(slotIds);
    Map<Long, List<SlotComponent>> componentsBySlotId = allSlotComponents.stream()
        .collect(Collectors.groupingBy(SlotComponent::getSlotId));

    List<Long> allComponentIds = allSlotComponents.stream()
        .map(SlotComponent::getComponentId)
        .distinct()
        .toList();

    Map<Long, Component> componentMap = allComponentIds.isEmpty()
        ? Map.of()
        : componentRepository.findByIdIn(allComponentIds).stream()
            .filter(c -> c.getStatus() == ComponentStatus.PUBLISHED)
            .collect(Collectors.toMap(Component::getId, c -> c));

    List<Long> publishedComponentIds = componentMap.keySet().stream().toList();

    Map<Long, ComponentI18n> componentI18nMap = publishedComponentIds.isEmpty()
        ? Map.of()
        : componentI18nRepository.findByComponentIdInAndLanguage(publishedComponentIds, lang)
            .stream()
            .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i));

    List<Long> typeIds = componentMap.values().stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .distinct()
        .toList();

    Map<Long, ComponentType> typeMap = typeIds.isEmpty()
        ? Map.of()
        : componentTypeRepository.findByIdIn(typeIds).stream()
            .collect(Collectors.toMap(ComponentType::getId, t -> t));

    Map<Long, List<ComponentEntry>> entriesByComponentId = publishedComponentIds.isEmpty()
        ? Map.of()
        : componentEntryRepository
            .findByComponentIdInAndStatusOrderBySortOrder(publishedComponentIds, ComponentStatus.PUBLISHED)
            .stream()
            .collect(Collectors.groupingBy(ComponentEntry::getComponentId));

    List<Long> entryIds = entriesByComponentId.values().stream()
        .flatMap(List::stream)
        .map(ComponentEntry::getId)
        .toList();

    Map<Long, ComponentEntryI18n> entryI18nMap = entryIds.isEmpty()
        ? Map.of()
        : componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds, lang)
            .stream()
            .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i));

    Map<Long, List<PageSlot>> slotsByPageId = allSlots.stream()
        .filter(s -> s.getPageId() != null)
        .collect(Collectors.groupingBy(PageSlot::getPageId));

    Map<String, PageDeliveryResponse> data = new LinkedHashMap<>();
    List<String> notFound = new ArrayList<>();

    for (String pageUid : limitedUids) {
      Page page = pageMap.get(pageUid);
      if (page == null) {
        notFound.add(pageUid);
        continue;
      }

      PageI18n i18n = pageI18nMap.get(page.getId());

      List<PageSlot> pageSlots = new ArrayList<>(sharedSlots);
      pageSlots.addAll(slotsByPageId.getOrDefault(page.getId(), List.of()));

      Map<String, List<ComponentDeliveryResponse>> slotsMap = buildSlotsMap(
          pageSlots, componentsBySlotId, componentMap, componentI18nMap, typeMap, entriesByComponentId, entryI18nMap);

      PageDeliveryResponse response = PageDeliveryResponse.builder()
          .uid(page.getUid())
          .title(i18n != null ? i18n.getTitle() : null)
          .subtitle(i18n != null ? i18n.getSubtitle() : null)
          .description(i18n != null ? i18n.getDescription() : null)
          .metaTitle(i18n != null ? i18n.getMetaTitle() : null)
          .metaDescription(i18n != null ? i18n.getMetaDescription() : null)
          .robotTag(page.getRobotTag())
          .urlPath(i18n != null ? i18n.getUrlPath() : null)
          .featuredImage(page.getFeaturedImage())
          .styleClasses(page.getStyleClasses())
          .slots(slotsMap)
          .build();

      data.put(pageUid, response);
    }

    return BatchPageDeliveryResponse.builder()
        .data(data)
        .meta(BatchPageDeliveryResponse.BatchMeta.builder()
            .requested(limitedUids.size())
            .found(data.size())
            .notFound(notFound)
            .build())
        .build();
  }

  private PageDeliveryResponse buildPageDeliveryResponse(Page page, Language lang) {
    Optional<PageI18n> i18nOpt = pageI18nRepository.findByPageIdAndLanguage(page.getId(), lang);

    List<PageSlot> pageSlots = pageSlotRepository.findByPageId(page.getId());
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();

    List<PageSlot> allSlots = new ArrayList<>(sharedSlots);
    allSlots.addAll(pageSlots);

    List<PageSlot> activeSlots = allSlots.stream()
        .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
        .toList();

    List<Long> slotIds = activeSlots.stream()
        .map(PageSlot::getId)
        .toList();

    List<SlotComponent> allSlotComponents = slotComponentRepository.findBySlotIdIn(slotIds);
    Map<Long, List<SlotComponent>> componentsBySlotId = allSlotComponents.stream()
        .collect(Collectors.groupingBy(SlotComponent::getSlotId));

    List<Long> allComponentIds = allSlotComponents.stream()
        .map(SlotComponent::getComponentId)
        .distinct()
        .toList();

    Map<Long, Component> componentMap = allComponentIds.isEmpty()
        ? Map.of()
        : componentRepository.findByIdIn(allComponentIds).stream()
            .filter(c -> c.getStatus() == ComponentStatus.PUBLISHED)
            .collect(Collectors.toMap(Component::getId, c -> c));

    List<Long> publishedComponentIds = componentMap.keySet().stream().toList();

    Map<Long, ComponentI18n> componentI18nMap = publishedComponentIds.isEmpty()
        ? Map.of()
        : componentI18nRepository.findByComponentIdInAndLanguage(publishedComponentIds, lang)
            .stream()
            .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i));

    List<Long> typeIds = componentMap.values().stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .distinct()
        .toList();

    Map<Long, ComponentType> typeMap = typeIds.isEmpty()
        ? Map.of()
        : componentTypeRepository.findByIdIn(typeIds).stream()
            .collect(Collectors.toMap(ComponentType::getId, t -> t));

    Map<Long, List<ComponentEntry>> entriesByComponentId = publishedComponentIds.isEmpty()
        ? Map.of()
        : componentEntryRepository
            .findByComponentIdInAndStatusOrderBySortOrder(publishedComponentIds, ComponentStatus.PUBLISHED)
            .stream()
            .collect(Collectors.groupingBy(ComponentEntry::getComponentId));

    List<Long> entryIds = entriesByComponentId.values().stream()
        .flatMap(List::stream)
        .map(ComponentEntry::getId)
        .toList();

    Map<Long, ComponentEntryI18n> entryI18nMap = entryIds.isEmpty()
        ? Map.of()
        : componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds, lang)
            .stream()
            .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i));

    Map<String, List<ComponentDeliveryResponse>> slotsMap = buildSlotsMap(
        activeSlots, componentsBySlotId, componentMap, componentI18nMap, typeMap, entriesByComponentId, entryI18nMap);

    PageI18n i18n = i18nOpt.orElse(null);

    return PageDeliveryResponse.builder()
        .uid(page.getUid())
        .title(i18n != null ? i18n.getTitle() : null)
        .subtitle(i18n != null ? i18n.getSubtitle() : null)
        .description(i18n != null ? i18n.getDescription() : null)
        .metaTitle(i18n != null ? i18n.getMetaTitle() : null)
        .metaDescription(i18n != null ? i18n.getMetaDescription() : null)
        .robotTag(page.getRobotTag())
        .urlPath(i18n != null ? i18n.getUrlPath() : null)
        .featuredImage(page.getFeaturedImage())
        .styleClasses(page.getStyleClasses())
        .slots(slotsMap)
        .build();
  }

  private Map<String, List<ComponentDeliveryResponse>> buildSlotsMap(
      List<PageSlot> slots,
      Map<Long, List<SlotComponent>> componentsBySlotId,
      Map<Long, Component> componentMap,
      Map<Long, ComponentI18n> componentI18nMap,
      Map<Long, ComponentType> typeMap,
      Map<Long, List<ComponentEntry>> entriesByComponentId,
      Map<Long, ComponentEntryI18n> entryI18nMap) {

    Map<String, List<ComponentDeliveryResponse>> slotsMap = new LinkedHashMap<>();

    for (PageSlot slot : slots) {
      List<SlotComponent> slotComps = componentsBySlotId.getOrDefault(slot.getId(), List.of());
      List<ComponentDeliveryResponse> compResponses = slotComps.stream()
          .filter(sc -> Boolean.TRUE.equals(sc.getIsVisible()))
          .sorted((a, b) -> Integer.compare(
              a.getSortOrder() != null ? a.getSortOrder() : 0,
              b.getSortOrder() != null ? b.getSortOrder() : 0))
          .filter(sc -> componentMap.containsKey(sc.getComponentId()))
          .map(sc -> buildComponentResponse(sc, componentMap, componentI18nMap, typeMap, entriesByComponentId,
              entryI18nMap))
          .toList();

      if (!compResponses.isEmpty()) {
        slotsMap.put(slot.getSlotName(), compResponses);
      }
    }

    return slotsMap;
  }

  private ComponentDeliveryResponse buildComponentResponse(
      SlotComponent sc,
      Map<Long, Component> componentMap,
      Map<Long, ComponentI18n> componentI18nMap,
      Map<Long, ComponentType> typeMap,
      Map<Long, List<ComponentEntry>> entriesByComponentId,
      Map<Long, ComponentEntryI18n> entryI18nMap) {

    Component comp = componentMap.get(sc.getComponentId());
    ComponentI18n compI18n = componentI18nMap.get(comp.getId());
    ComponentType type = comp.getComponentTypeId() != null
        ? typeMap.get(comp.getComponentTypeId())
        : null;

    List<ComponentEntry> entries = entriesByComponentId.getOrDefault(comp.getId(), List.of());
    List<EntryDeliveryResponse> entryResponses = entries.stream()
        .map(entry -> buildEntryResponseOptimized(entry, entryI18nMap.get(entry.getId())))
        .toList();

    return ComponentDeliveryResponse.builder()
        .uid(comp.getUid())
        .type(type != null ? type.getName() : null)
        .category(type != null ? type.getCategory() : null)
        .title(compI18n != null ? compI18n.getTitle() : null)
        .subtitle(compI18n != null ? compI18n.getSubtitle() : null)
        .description(compI18n != null ? compI18n.getDescription() : null)
        .isVisible(comp.getIsVisible())
        .styleClasses(comp.getStyleClasses())
        .entries(entryResponses)
        .build();
  }

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
