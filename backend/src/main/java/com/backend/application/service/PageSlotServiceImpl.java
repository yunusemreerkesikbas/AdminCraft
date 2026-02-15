package com.backend.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.PageSlotCommands.AddComponentToSlotCommand;
import com.backend.application.command.PageSlotCommands.CreatePageSlotCommand;
import com.backend.application.command.PageSlotCommands.ReorderSlotComponentsCommand;
import com.backend.application.dto.request.UpdatePageSlotRequest;
import com.backend.application.dto.slot.PageSlotDto;
import com.backend.application.dto.slot.SlotComponentDto;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.backend.domain.repository.TemplateSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageSlotServiceImpl implements PageSlotService {

  private final PageSlotRepository pageSlotRepository;
  private final SlotComponentRepository slotComponentRepository;
  private final PageRepository pageRepository;
  private final ComponentRepository componentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final TemplateSlotRepository templateSlotRepository;

  @Override
  public PageSlotDto createSlot(Long pageId, CreatePageSlotCommand command) {
    boolean isShared = Boolean.TRUE.equals(command.isShared());
    Long effectivePageId = isShared ? null : pageId;

    if (effectivePageId != null) {
      Page page = pageRepository.findById(effectivePageId)
          .orElseThrow(() -> new IllegalArgumentException("Page not found: " + effectivePageId));
      if (page.getTemplateId() != null) {
        throw new IllegalArgumentException(
            "Cannot create slots on a template-based page. Manage slot structure from page template.");
      }
    }

    if (isShared) {
      pageSlotRepository.findSharedSlotBySlotName(command.slotName())
          .ifPresent(existing -> {
            throw new IllegalArgumentException(
                "Shared slot with name '" + command.slotName() + "' already exists");
          });
    } else if (pageSlotRepository.existsByPageIdAndSlotName(effectivePageId, command.slotName())) {
      throw new IllegalArgumentException(
          "Slot with name '" + command.slotName() + "' already exists for this page");
    }

    PageSlot slot = new PageSlot();
    slot.setPageId(effectivePageId);
    if (command.uid() != null && !command.uid().isBlank()) {
      pageSlotRepository.findByUid(command.uid())
          .ifPresent(existing -> {
            throw new IllegalArgumentException("Slot uid already exists: " + command.uid());
          });
      slot.setUid(command.uid());
    }
    slot.setSlotName(command.slotName());
    slot.setPosition(command.position());
    slot.setSortOrder(command.sortOrder() != null ? command.sortOrder() : 0);
    slot.setIsActive(true);
    slot.setIsShared(isShared);

    PageSlot savedSlot = pageSlotRepository.save(slot);
    log.info("Created slot '{}' for page {}", command.slotName(), effectivePageId);

    return mapToDto(savedSlot, List.of());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSlotDto> getSlotsByPageId(Long pageId) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));

    List<PageSlot> pageSlots = pageSlotRepository.findByPageId(pageId);
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();

    if (page.getTemplateId() == null) {
      return mapSlotsToDtos(mergeSlotsWithoutTemplate(pageSlots, sharedSlots));
    }

    List<TemplateSlot> templateSlots = templateSlotRepository.findByTemplateId(page.getTemplateId());
    if (templateSlots.isEmpty()) {
      return mapSlotsToDtos(mergeSlotsWithoutTemplate(pageSlots, sharedSlots));
    }

    return mapSlotsToDtos(resolveEffectiveTemplateSlots(pageId, templateSlots, pageSlots, sharedSlots));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSlotDto> getSharedSlots() {
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();
    return mapSlotsToDtos(sharedSlots);
  }

  @Override
  public PageSlotDto updateSlot(Long pageId, String slotName, UpdatePageSlotRequest request) {
    PageSlot slot = findWritablePageSlot(pageId, slotName);
    assertTemplateSlotMutationAllowed(pageId, slotName, request.getSlotName());

    if (request.getIsShared() != null && !Objects.equals(slot.getIsShared(), request.getIsShared())) {
      throw new IllegalArgumentException("Shared flag cannot be changed for an existing slot");
    }

    String updatedSlotName = request.getSlotName();
    if (!Objects.equals(slot.getSlotName(), updatedSlotName) && isSlotNameTaken(slot, updatedSlotName)) {
      throw new IllegalArgumentException("Slot with name '" + updatedSlotName + "' already exists");
    }

    String updatedUid = request.getUid();
    if (updatedUid != null && !updatedUid.isBlank() && !Objects.equals(slot.getUid(), updatedUid)) {
      pageSlotRepository.findByUid(updatedUid)
          .ifPresent(existing -> {
            throw new IllegalArgumentException("Slot uid already exists: " + updatedUid);
          });
      slot.setUid(updatedUid);
    }

    slot.setSlotName(updatedSlotName);
    slot.setPosition(request.getPosition());
    if (request.getSortOrder() != null) {
      slot.setSortOrder(request.getSortOrder());
    }
    if (request.getIsActive() != null) {
      slot.setIsActive(request.getIsActive());
    }

    PageSlot savedSlot = pageSlotRepository.save(slot);
    log.info("Updated slot '{}' for page {}", slotName, pageId);

    return mapSlotsToDtos(List.of(savedSlot)).stream()
        .findFirst()
        .orElseGet(() -> mapToDto(savedSlot, List.of()));
  }

  @Override
  @Transactional
  public void deleteSlot(Long pageId, String slotName) {
    assertTemplateSlotMutationAllowed(pageId, slotName, null);

    PageSlot slot = pageSlotRepository.findByPageIdAndSlotName(pageId, slotName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Slot '" + slotName + "' not found for page " + pageId));
    slotComponentRepository.deleteBySlotId(slot.getId());
    pageSlotRepository.delete(slot);
    log.info("Deleted slot '{}' from page {}", slotName, pageId);
  }

  @Override
  public void addComponentToSlot(Long pageId, String slotName, AddComponentToSlotCommand command) {
    PageSlot slot = findOrMaterializeWritablePageSlot(pageId, slotName, true);
    componentRepository.findById(command.componentId())
        .orElseThrow(() -> new IllegalArgumentException("Component not found: " + command.componentId()));
    if (slotComponentRepository.existsBySlotIdAndComponentId(slot.getId(), command.componentId())) {
      throw new IllegalArgumentException("Component already exists in this slot");
    }
    int nextSortOrder = slotComponentRepository.findMaxSortOrderBySlotId(slot.getId())
        .orElse(-1) + 1;

    SlotComponent slotComponent = new SlotComponent();
    slotComponent.setSlotId(slot.getId());
    slotComponent.setComponentId(command.componentId());
    slotComponent.setSortOrder(nextSortOrder);
    slotComponent.setIsVisible(true);

    slotComponentRepository.save(slotComponent);
    log.info("Added component {} to slot '{}' in page {}", command.componentId(), slotName, pageId);
  }

  @Override
  public void removeComponentFromSlot(Long pageId, String slotName, Long componentId) {
    PageSlot slot = findOrMaterializeWritablePageSlot(pageId, slotName, true);

    SlotComponent slotComponent = slotComponentRepository
        .findBySlotIdAndComponentId(slot.getId(), componentId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Component " + componentId + " not found in slot '" + slotName + "'"));

    slotComponentRepository.delete(slotComponent);
    log.info("Removed component {} from slot '{}' in page {}", componentId, slotName, pageId);
  }

  @Override
  public void reorderComponents(Long pageId, String slotName, ReorderSlotComponentsCommand command) {
    PageSlot slot = findOrMaterializeWritablePageSlot(pageId, slotName, true);

    List<SlotComponent> slotComponents = slotComponentRepository
        .findBySlotIdOrderBySortOrder(slot.getId());

    Map<Long, SlotComponent> componentMap = slotComponents.stream()
        .collect(Collectors.toMap(SlotComponent::getComponentId, sc -> sc));
    List<SlotComponent> updatedComponents = new ArrayList<>();
    for (int i = 0; i < command.componentIds().size(); i++) {
      Long componentId = command.componentIds().get(i);
      SlotComponent sc = componentMap.get(componentId);
      if (sc != null) {
        sc.setSortOrder(i);
        updatedComponents.add(sc);
      }
    }

    slotComponentRepository.saveAll(updatedComponents);
    log.info("Reordered {} components in slot '{}' for page {}",
        command.componentIds().size(), slotName, pageId);
  }

  private PageSlot findWritablePageSlot(Long pageId, String slotName) {
    return pageSlotRepository.findByPageIdAndSlotName(pageId, slotName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Slot '" + slotName + "' not materialized for page " + pageId));
  }

  private PageSlot findOrMaterializeWritablePageSlot(Long pageId, String slotName, boolean copySharedComponents) {
    Optional<PageSlot> existing = pageSlotRepository.findByPageIdAndSlotName(pageId, slotName);
    if (existing.isPresent()) {
      return existing.get();
    }

    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));

    String position;
    Integer sortOrder;
    if (page.getTemplateId() != null) {
      TemplateSlot templateSlot = templateSlotRepository.findByTemplateIdAndSlotName(page.getTemplateId(), slotName)
          .orElseThrow(() -> new IllegalArgumentException(
              "Slot '" + slotName + "' is not defined in page template"));
      position = templateSlot.getPosition();
      sortOrder = templateSlot.getSortOrder();
    } else {
      PageSlot sharedSlot = pageSlotRepository.findSharedSlotBySlotName(slotName)
          .orElseThrow(() -> new IllegalArgumentException(
              "Slot '" + slotName + "' not materialized for page " + pageId));
      position = sharedSlot.getPosition();
      sortOrder = sharedSlot.getSortOrder();
    }

    PageSlot materialized = new PageSlot();
    materialized.setPageId(pageId);
    materialized.setSlotName(slotName);
    materialized.setPosition(position);
    materialized.setSortOrder(sortOrder != null ? sortOrder : 0);
    materialized.setIsActive(true);
    materialized.setIsShared(false);

    PageSlot savedSlot = pageSlotRepository.save(materialized);
    if (copySharedComponents) {
      copySharedComponentsToPageSlot(slotName, savedSlot.getId());
    }

    log.info("Materialized slot '{}' for page {}", slotName, pageId);
    return savedSlot;
  }

  private void copySharedComponentsToPageSlot(String slotName, Long targetSlotId) {
    Optional<PageSlot> sharedSlotOpt = pageSlotRepository.findSharedSlotBySlotName(slotName);
    if (sharedSlotOpt.isEmpty()) {
      return;
    }

    List<SlotComponent> sharedComponents = slotComponentRepository
        .findBySlotIdOrderBySortOrder(sharedSlotOpt.get().getId());
    if (sharedComponents.isEmpty()) {
      return;
    }

    List<SlotComponent> copies = new ArrayList<>();
    for (SlotComponent sharedComponent : sharedComponents) {
      SlotComponent copy = new SlotComponent();
      copy.setSlotId(targetSlotId);
      copy.setComponentId(sharedComponent.getComponentId());
      copy.setSortOrder(sharedComponent.getSortOrder());
      copy.setIsVisible(sharedComponent.getIsVisible());
      copies.add(copy);
    }
    slotComponentRepository.saveAll(copies);
  }

  private boolean isSlotNameTaken(PageSlot slot, String updatedSlotName) {
    if (Boolean.TRUE.equals(slot.getIsShared())) {
      return pageSlotRepository.findSharedSlotBySlotName(updatedSlotName)
          .filter(existing -> !existing.getId().equals(slot.getId()))
          .isPresent();
    }
    return pageSlotRepository.findByPageIdAndSlotName(slot.getPageId(), updatedSlotName)
        .filter(existing -> !existing.getId().equals(slot.getId()))
        .isPresent();
  }

  private void assertTemplateSlotMutationAllowed(Long pageId, String currentSlotName, String updatedSlotName) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));
    Long templateId = page.getTemplateId();
    if (templateId == null) {
      return;
    }

    boolean currentIsTemplateSlot = templateSlotRepository.existsByTemplateIdAndSlotName(templateId, currentSlotName);
    boolean targetIsTemplateSlot = updatedSlotName != null
        && templateSlotRepository.existsByTemplateIdAndSlotName(templateId, updatedSlotName);
    if (currentIsTemplateSlot || targetIsTemplateSlot) {
      throw new IllegalArgumentException(
          "Template-managed slots cannot be modified from page endpoint. Use page template slot APIs.");
    }
  }

  private List<PageSlot> mergeSlotsWithoutTemplate(List<PageSlot> pageSlots, List<PageSlot> sharedSlots) {
    // Page-specific slot overrides shared slot with same slotName.
    Map<String, PageSlot> bySlotName = new LinkedHashMap<>();
    for (PageSlot shared : sharedSlots) {
      bySlotName.putIfAbsent(shared.getSlotName(), shared);
    }
    for (PageSlot pageSlot : pageSlots) {
      bySlotName.put(pageSlot.getSlotName(), pageSlot);
    }
    return bySlotName.values().stream()
        .sorted(Comparator
            .comparingInt((PageSlot slot) -> slot.getSortOrder() != null ? slot.getSortOrder() : 0)
            .thenComparing(PageSlot::getSlotName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  private List<PageSlot> resolveEffectiveTemplateSlots(
      Long pageId,
      List<TemplateSlot> templateSlots,
      List<PageSlot> pageSlots,
      List<PageSlot> sharedSlots) {
    Map<String, PageSlot> pageBySlotName = pageSlots.stream()
        .collect(Collectors.toMap(PageSlot::getSlotName, slot -> slot, (first, second) -> first));
    Map<String, PageSlot> sharedBySlotName = sharedSlots.stream()
        .collect(Collectors.toMap(PageSlot::getSlotName, slot -> slot, (first, second) -> first));

    List<PageSlot> effectiveSlots = new ArrayList<>();
    for (TemplateSlot templateSlot : templateSlots) {
      PageSlot source = pageBySlotName.get(templateSlot.getSlotName());
      if (source == null) {
        source = sharedBySlotName.get(templateSlot.getSlotName());
      }
      effectiveSlots.add(buildEffectiveSlot(pageId, templateSlot, source));
    }
    return effectiveSlots;
  }

  private PageSlot buildEffectiveSlot(Long pageId, TemplateSlot templateSlot, PageSlot source) {
    PageSlot effective = new PageSlot();
    if (source != null) {
      effective.setId(source.getId());
      effective.setUuid(source.getUuid());
      effective.setUid(source.getUid());
      effective.setCreatedAt(source.getCreatedAt());
      effective.setUpdatedAt(source.getUpdatedAt());
      effective.setCreatedBy(source.getCreatedBy());
      effective.setUpdatedBy(source.getUpdatedBy());
      effective.setIsActive(source.getIsActive());
      effective.setIsShared(source.getIsShared());
    } else {
      effective.setUid("template-slot-" + pageId + "-" + templateSlot.getId());
      effective.setIsActive(true);
      effective.setIsShared(false);
    }

    effective.setPageId(pageId);
    effective.setSlotName(templateSlot.getSlotName());
    effective.setPosition(templateSlot.getPosition());
    effective.setSortOrder(templateSlot.getSortOrder());

    return effective;
  }

  private List<PageSlotDto> mapSlotsToDtos(List<PageSlot> slots) {
    if (slots.isEmpty()) {
      return List.of();
    }
    List<Long> slotIds = slots.stream()
        .map(PageSlot::getId)
        .filter(Objects::nonNull)
        .toList();
    List<SlotComponent> allSlotComponents = slotIds.isEmpty()
        ? List.of()
        : slotComponentRepository.findBySlotIdIn(slotIds);

    Map<Long, List<SlotComponent>> componentsBySlotId = allSlotComponents.stream()
        .collect(Collectors.groupingBy(SlotComponent::getSlotId));

    List<Long> componentIds = allSlotComponents.stream()
        .map(SlotComponent::getComponentId)
        .distinct()
        .toList();

    Map<Long, Component> componentMap = componentIds.isEmpty()
        ? Map.of()
        : componentRepository.findByIdIn(componentIds).stream()
            .collect(Collectors.toMap(Component::getId, c -> c));

    List<Long> typeIds = componentMap.values().stream()
        .map(Component::getComponentTypeId)
        .filter(id -> id != null)
        .distinct()
        .toList();

    Map<Long, ComponentType> typeMap = typeIds.isEmpty()
        ? Map.of()
        : componentTypeRepository.findByIdIn(typeIds).stream()
            .collect(Collectors.toMap(ComponentType::getId, t -> t));
    return slots.stream()
        .map(slot -> {
          List<SlotComponent> slotComponents = componentsBySlotId
              .getOrDefault(slot.getId(), List.of());
          List<SlotComponentDto> componentDtos = slotComponents.stream()
              .sorted((a, b) -> Integer.compare(
                  a.getSortOrder() != null ? a.getSortOrder() : 0,
                  b.getSortOrder() != null ? b.getSortOrder() : 0))
              .map(sc -> mapToComponentDto(sc, componentMap, typeMap))
              .toList();
          return mapToDto(slot, componentDtos);
        })
        .toList();
  }

  private PageSlotDto mapToDto(PageSlot slot, List<SlotComponentDto> components) {
    return new PageSlotDto(
        slot.getId(),
        slot.getUid(),
        slot.getSlotName(),
        slot.getPosition(),
        slot.getSortOrder(),
        slot.getIsActive(),
        slot.getIsShared(),
        slot.getCreatedAt(),
        slot.getUpdatedAt(),
        components);
  }

  private SlotComponentDto mapToComponentDto(
      SlotComponent sc,
      Map<Long, Component> componentMap,
      Map<Long, ComponentType> typeMap) {

    Component component = componentMap.get(sc.getComponentId());
    String typeName = null;

    if (component != null && component.getComponentTypeId() != null) {
      ComponentType type = typeMap.get(component.getComponentTypeId());
      if (type != null) {
        typeName = type.getName();
      }
    }

    return new SlotComponentDto(
        sc.getId(),
        sc.getComponentId(),
        component != null ? component.getUid() : null,
        component != null ? component.getName() : null,
        typeName,
        sc.getSortOrder(),
        sc.getIsVisible());
  }
}
