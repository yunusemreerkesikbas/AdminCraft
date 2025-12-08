package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.backend.presentation.dto.request.CreatePageSlotRequest;
import com.backend.presentation.dto.response.PageSlotResponse;
import com.backend.presentation.dto.response.SlotComponentResponse;

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

  @Override
  public PageSlotResponse createSlot(Long pageId, CreatePageSlotRequest request) {
    if (pageId != null) {
      pageRepository.findById(pageId)
          .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));
    }
    if (pageSlotRepository.existsByPageIdAndSlotName(pageId, request.getSlotName())) {
      throw new IllegalArgumentException(
          "Slot with name '" + request.getSlotName() + "' already exists for this page");
    }

    PageSlot slot = new PageSlot();
    slot.setPageId(pageId);
    slot.setSlotName(request.getSlotName());
    slot.setPosition(request.getPosition());
    slot.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
    slot.setIsActive(true);
    slot.setIsShared(request.getIsShared() != null ? request.getIsShared() : false);
    if (Boolean.TRUE.equals(slot.getIsShared())) {
      slot.setPageId(null);
    }

    PageSlot savedSlot = pageSlotRepository.save(slot);
    log.info("Created slot '{}' for page {}", request.getSlotName(), pageId);

    return mapToResponse(savedSlot, List.of());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSlotResponse> getSlotsByPageId(Long pageId) {
    List<PageSlot> pageSlots = pageSlotRepository.findByPageId(pageId);

    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();

    List<PageSlot> allSlots = new ArrayList<>();
    allSlots.addAll(sharedSlots);
    allSlots.addAll(pageSlots);

    return mapSlotsToResponses(allSlots);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSlotResponse> getSharedSlots() {
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();
    return mapSlotsToResponses(sharedSlots);
  }

  @Override
  public void deleteSlot(Long pageId, String slotName) {
    PageSlot slot = pageSlotRepository.findByPageIdAndSlotName(pageId, slotName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Slot '" + slotName + "' not found for page " + pageId));
    slotComponentRepository.deleteBySlotId(slot.getId());
    pageSlotRepository.delete(slot);
    log.info("Deleted slot '{}' from page {}", slotName, pageId);
  }

  @Override
  public void addComponentToSlot(Long pageId, String slotName, Long componentId) {
    PageSlot slot = findSlot(pageId, slotName);
    componentRepository.findById(componentId)
        .orElseThrow(() -> new IllegalArgumentException("Component not found: " + componentId));
    if (slotComponentRepository.existsBySlotIdAndComponentId(slot.getId(), componentId)) {
      throw new IllegalArgumentException("Component already exists in this slot");
    }
    int nextSortOrder = slotComponentRepository.findMaxSortOrderBySlotId(slot.getId())
        .orElse(-1) + 1;

    SlotComponent slotComponent = new SlotComponent();
    slotComponent.setSlotId(slot.getId());
    slotComponent.setComponentId(componentId);
    slotComponent.setSortOrder(nextSortOrder);
    slotComponent.setIsVisible(true);

    slotComponentRepository.save(slotComponent);
    log.info("Added component {} to slot '{}' in page {}", componentId, slotName, pageId);
  }

  @Override
  public void removeComponentFromSlot(Long pageId, String slotName, Long componentId) {
    PageSlot slot = findSlot(pageId, slotName);

    SlotComponent slotComponent = slotComponentRepository
        .findBySlotIdAndComponentId(slot.getId(), componentId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Component " + componentId + " not found in slot '" + slotName + "'"));

    slotComponentRepository.delete(slotComponent);
    log.info("Removed component {} from slot '{}' in page {}", componentId, slotName, pageId);
  }

  @Override
  public void reorderComponents(Long pageId, String slotName, List<Long> componentIds) {
    PageSlot slot = findSlot(pageId, slotName);

    List<SlotComponent> slotComponents = slotComponentRepository
        .findBySlotIdOrderBySortOrder(slot.getId());

    Map<Long, SlotComponent> componentMap = slotComponents.stream()
        .collect(Collectors.toMap(SlotComponent::getComponentId, sc -> sc));
    List<SlotComponent> updatedComponents = new ArrayList<>();
    for (int i = 0; i < componentIds.size(); i++) {
      Long componentId = componentIds.get(i);
      SlotComponent sc = componentMap.get(componentId);
      if (sc != null) {
        sc.setSortOrder(i);
        updatedComponents.add(sc);
      }
    }

    slotComponentRepository.saveAll(updatedComponents);
    log.info("Reordered {} components in slot '{}' for page {}",
        componentIds.size(), slotName, pageId);
  }

  private PageSlot findSlot(Long pageId, String slotName) {
    Optional<PageSlot> slot = pageSlotRepository.findByPageIdAndSlotName(pageId, slotName);
    if (slot.isPresent()) {
      return slot.get();
    }
    return pageSlotRepository.findByPageIdAndSlotName(null, slotName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Slot '" + slotName + "' not found for page " + pageId));
  }

  private List<PageSlotResponse> mapSlotsToResponses(List<PageSlot> slots) {
    if (slots.isEmpty()) {
      return List.of();
    }
    List<Long> slotIds = slots.stream().map(PageSlot::getId).toList();
    List<SlotComponent> allSlotComponents = slotComponentRepository.findBySlotIdIn(slotIds);

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
        : typeIds.stream()
            .map(componentTypeRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toMap(ComponentType::getId, t -> t));
    return slots.stream()
        .map(slot -> {
          List<SlotComponent> slotComponents = componentsBySlotId
              .getOrDefault(slot.getId(), List.of());
          List<SlotComponentResponse> componentResponses = slotComponents.stream()
              .sorted((a, b) -> Integer.compare(
                  a.getSortOrder() != null ? a.getSortOrder() : 0,
                  b.getSortOrder() != null ? b.getSortOrder() : 0))
              .map(sc -> mapToComponentResponse(sc, componentMap, typeMap))
              .toList();
          return mapToResponse(slot, componentResponses);
        })
        .toList();
  }

  private PageSlotResponse mapToResponse(PageSlot slot, List<SlotComponentResponse> components) {
    return PageSlotResponse.builder()
        .id(slot.getId())
        .uid(slot.getUid())
        .slotName(slot.getSlotName())
        .position(slot.getPosition())
        .sortOrder(slot.getSortOrder())
        .isActive(slot.getIsActive())
        .isShared(slot.getIsShared())
        .createdAt(slot.getCreatedAt())
        .updatedAt(slot.getUpdatedAt())
        .components(components)
        .build();
  }

  private SlotComponentResponse mapToComponentResponse(
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

    return SlotComponentResponse.builder()
        .id(sc.getId())
        .componentId(sc.getComponentId())
        .componentUid(component != null ? component.getUid() : null)
        .componentName(component != null ? component.getName() : null)
        .componentTypeName(typeName)
        .sortOrder(sc.getSortOrder())
        .isVisible(sc.getIsVisible())
        .build();
  }
}
