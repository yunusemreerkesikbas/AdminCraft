package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.PageSlotCommands.AddComponentToSlotCommand;
import com.backend.application.command.PageSlotCommands.CreatePageSlotCommand;
import com.backend.application.command.PageSlotCommands.ReorderSlotComponentsCommand;
import com.backend.application.dto.slot.PageSlotDto;
import com.backend.application.dto.slot.SlotComponentDto;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.SlotComponentRepository;

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
  public PageSlotDto createSlot(Long pageId, CreatePageSlotCommand command) {
    boolean isShared = Boolean.TRUE.equals(command.isShared());
    Long effectivePageId = isShared ? null : pageId;

    if (effectivePageId != null) {
      pageRepository.findById(effectivePageId)
          .orElseThrow(() -> new IllegalArgumentException("Page not found: " + effectivePageId));
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
    List<PageSlot> pageSlots = pageSlotRepository.findByPageId(pageId);

    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();

    List<PageSlot> allSlots = new ArrayList<>();
    allSlots.addAll(sharedSlots);
    allSlots.addAll(pageSlots);

    return mapSlotsToDtos(allSlots);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSlotDto> getSharedSlots() {
    List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();
    return mapSlotsToDtos(sharedSlots);
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
  public void addComponentToSlot(Long pageId, String slotName, AddComponentToSlotCommand command) {
    PageSlot slot = findSlot(pageId, slotName);
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
    PageSlot slot = findSlot(pageId, slotName);

    SlotComponent slotComponent = slotComponentRepository
        .findBySlotIdAndComponentId(slot.getId(), componentId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Component " + componentId + " not found in slot '" + slotName + "'"));

    slotComponentRepository.delete(slotComponent);
    log.info("Removed component {} from slot '{}' in page {}", componentId, slotName, pageId);
  }

  @Override
  public void reorderComponents(Long pageId, String slotName, ReorderSlotComponentsCommand command) {
    PageSlot slot = findSlot(pageId, slotName);

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

  private PageSlot findSlot(Long pageId, String slotName) {
    Optional<PageSlot> slot = pageSlotRepository.findByPageIdAndSlotName(pageId, slotName);
    if (slot.isPresent()) {
      return slot.get();
    }
    return pageSlotRepository.findByPageIdAndSlotName(null, slotName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Slot '" + slotName + "' not found for page " + pageId));
  }

  private List<PageSlotDto> mapSlotsToDtos(List<PageSlot> slots) {
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
    return PageSlotDto.builder()
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

    return SlotComponentDto.builder()
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
