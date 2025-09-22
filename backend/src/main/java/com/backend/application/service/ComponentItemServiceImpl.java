package com.backend.application.service;

import com.backend.domain.entity.*;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.*;
import com.backend.presentation.dto.request.NavbarItemRequest;
import com.backend.presentation.dto.request.NavbarItemsReorderRequest;
import com.backend.presentation.dto.response.NavbarItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComponentItemServiceImpl implements ComponentItemService {

  private final ComponentRepository componentRepository;
  private final ComponentItemRepository itemRepository;
  private final ComponentTranslationRepository componentTranslationRepository;
  private final com.backend.domain.repository.ComponentItemTranslationRepository itemTranslationRepository;
  private final LanguageService languageService;

  public ComponentItemServiceImpl(
      ComponentRepository componentRepository,
      ComponentItemRepository itemRepository,
      ComponentTranslationRepository componentTranslationRepository,
      com.backend.domain.repository.ComponentItemTranslationRepository itemTranslationRepository,
      LanguageService languageService) {
    this.componentRepository = componentRepository;
    this.itemRepository = itemRepository;
    this.componentTranslationRepository = componentTranslationRepository;
    this.itemTranslationRepository = itemTranslationRepository;
    this.languageService = languageService;
  }

  @Override
  public List<NavbarItemResponse> listTree(Long tenantId, Long componentId) {
    Component component = componentRepository.findByIdAndTenantId(componentId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("ui.component.not.found"));

    List<ComponentItem> all = itemRepository.findAllByComponentId(componentId);
    if (all.isEmpty())
      return List.of();

    Set<Language> langs = languageService.getSupportedLanguages(tenantId);
    List<Long> itemIds = all.stream().map(ComponentItem::getId).toList();
    java.util.List<ComponentItemTranslation> translations = itemTranslationRepository
        .findAllByItemIdInAndLanguageIn(itemIds, new java.util.ArrayList<>(langs));

    Map<Long, Map<String, NavbarItemResponse.I18n>> trMap = new HashMap<>();
    for (ComponentItemTranslation t : translations) {
      trMap.computeIfAbsent(t.getItem().getId(), k -> new HashMap<>())
          .put(t.getLanguage().getCode().toLowerCase(),
              new NavbarItemResponse.I18n(
                  t.getTitle(), t.getSubtitle(), t.getUrl(),
                  t.getSeoTitle(), t.getSeoDescription(), t.getSeoKeywords()));
    }

    Map<Long, List<ComponentItem>> childrenByParent = all.stream()
        .filter(i -> i.getParent() != null)
        .collect(Collectors.groupingBy(i -> i.getParent().getId()));

    List<ComponentItem> roots = all.stream()
        .filter(i -> i.getParent() == null)
        .sorted(Comparator.comparing(ComponentItem::getSortOrder).thenComparing(ComponentItem::getId))
        .toList();

    return roots.stream()
        .map(r -> mapTree(r, childrenByParent, trMap))
        .toList();
  }

  private NavbarItemResponse mapTree(
      ComponentItem node,
      Map<Long, List<ComponentItem>> childrenByParent,
      Map<Long, Map<String, NavbarItemResponse.I18n>> trMap) {
    List<NavbarItemResponse> children = childrenByParent.getOrDefault(node.getId(), List.of())
        .stream()
        .sorted(Comparator.comparing(ComponentItem::getSortOrder).thenComparing(ComponentItem::getId))
        .map(child -> mapTree(child, childrenByParent, trMap))
        .toList();

    return new NavbarItemResponse(
        node.getId(),
        node.getUid(),
        node.getUuid(),
        node.getParent() == null ? null : node.getParent().getId(),
        node.getLevel(),
        node.isVisible(),
        node.getSortOrder(),
        trMap.getOrDefault(node.getId(), Map.of()),
        children);
  }

  @Override
  @Transactional
  public NavbarItemResponse create(Long tenantId, Long componentId, NavbarItemRequest request) {
    Component component = componentRepository.findByIdAndTenantId(componentId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("ui.component.not.found"));

    if (itemRepository.existsByComponentIdAndUid(componentId, request.uid())) {
      throw new IllegalArgumentException("ui.navbar.uid.conflict");
    }

    ComponentItem item = new ComponentItem();
    item.setComponent(component);
    item.setUid(request.uid());
    item.setVisible(Boolean.TRUE.equals(request.visible()));
    if (request.sortOrder() != null)
      item.setSortOrder(request.sortOrder());

    if (request.parentId() != null) {
      ComponentItem parent = itemRepository.findByIdAndComponentId(request.parentId(), componentId)
          .orElseThrow(() -> new IllegalArgumentException("ui.navbar.parent.not.found"));
      int level = parent.getLevel() == null ? 1 : parent.getLevel() + 1;
      if (level > 3)
        throw new IllegalArgumentException("ui.navbar.level.exceeded");
      item.setParent(parent);
      item.setLevel(level);
    } else {
      item.setLevel(1);
    }

    var saved = itemRepository.save(item);

    upsertItemTranslations(tenantId, saved, request);

    return listTree(tenantId, componentId).stream()
        .filter(n -> Objects.equals(n.id(), saved.getId()))
        .findFirst()
        .orElseThrow();
  }

  @Override
  @Transactional
  public NavbarItemResponse update(Long tenantId, Long componentId, Long itemId, NavbarItemRequest request) {
    Component component = componentRepository.findByIdAndTenantId(componentId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("ui.component.not.found"));

    ComponentItem item = itemRepository.findByIdAndComponentId(itemId, componentId)
        .orElseThrow(() -> new IllegalArgumentException("ui.navbar.item.not.found"));

    if (request.visible() != null)
      item.setVisible(request.visible());
    if (request.sortOrder() != null)
      item.setSortOrder(request.sortOrder());

    if (request.parentId() != null && (item.getParent() == null ||
        !Objects.equals(item.getParent().getId(), request.parentId()))) {
      ComponentItem parent = itemRepository.findByIdAndComponentId(request.parentId(), componentId)
          .orElseThrow(() -> new IllegalArgumentException("ui.navbar.parent.not.found"));
      int level = parent.getLevel() == null ? 1 : parent.getLevel() + 1;
      if (level > 3)
        throw new IllegalArgumentException("ui.navbar.level.exceeded");
      validateNoCycle(item, parent);
      item.setParent(parent);
      item.setLevel(level);
    }

    var saved = itemRepository.save(item);

    upsertItemTranslations(tenantId, saved, request);

    return listTree(tenantId, componentId).stream()
        .filter(n -> Objects.equals(n.id(), saved.getId()))
        .findFirst()
        .orElseThrow();
  }

  @Override
  @Transactional
  public void delete(Long tenantId, Long componentId, Long itemId) {
    Component component = componentRepository.findByIdAndTenantId(componentId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("ui.component.not.found"));

    ComponentItem item = itemRepository.findByIdAndComponentId(itemId, componentId)
        .orElseThrow(() -> new IllegalArgumentException("ui.navbar.item.not.found"));
    itemRepository.delete(item);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void reorder(Long tenantId, Long componentId, NavbarItemsReorderRequest request) {
    if (request == null || request.changes() == null || request.changes().isEmpty())
      return;
    for (var ch : request.changes()) {
      ComponentItem item = itemRepository.findByIdAndComponentId(ch.itemId(), componentId)
          .orElseThrow(() -> new IllegalArgumentException("ui.navbar.item.not.found"));
      if (ch.parentId() != null) {
        ComponentItem parent = itemRepository.findByIdAndComponentId(ch.parentId(), componentId)
            .orElseThrow(() -> new IllegalArgumentException("ui.navbar.parent.not.found"));
        validateNoCycle(item, parent);
        int level = parent.getLevel() == null ? 1 : parent.getLevel() + 1;
        if (level > 3)
          throw new IllegalArgumentException("ui.navbar.level.exceeded");
        validateDescendantDepth(item, level);
        item.setParent(parent);
        item.setLevel(level);
      } else {
        item.setParent(null);
        item.setLevel(1);
      }
      if (ch.sortOrder() != null)
        item.setSortOrder(ch.sortOrder());
      itemRepository.save(item);
    }
  }

  /**
   * Validates that setting newParent as parent of item won't create a cycle
   * by traversing the parent chain of newParent to ensure item is not in it.
   */
  private void validateNoCycle(ComponentItem item, ComponentItem newParent) {
    ComponentItem current = newParent;
    Set<Long> visited = new HashSet<>();

    while (current != null) {
      if (Objects.equals(current.getId(), item.getId())) {
        throw new IllegalArgumentException("ui.navbar.parent.cycle");
      }

      // Prevent infinite loops in case of existing data corruption
      if (visited.contains(current.getId())) {
        throw new IllegalArgumentException("ui.navbar.parent.cycle.detected");
      }
      visited.add(current.getId());

      current = current.getParent();
    }
  }

  /**
   * Validates that moving item to newLevel won't cause its descendants
   * to exceed the maximum level limit of 3.
   */
  private void validateDescendantDepth(ComponentItem item, int newLevel) {
    if (newLevel > 3) {
      throw new IllegalArgumentException("ui.navbar.level.exceeded");
    }

    // Find maximum depth of descendants
    int maxDescendantDepth = findMaxDescendantDepth(item);
    if (newLevel + maxDescendantDepth > 3) {
      throw new IllegalArgumentException("ui.navbar.descendant.level.exceeded");
    }
  }

  /**
   * Recursively finds the maximum depth of descendants for the given item.
   */
  private int findMaxDescendantDepth(ComponentItem item) {
    List<ComponentItem> children = itemRepository.findByParentId(item.getId());
    if (children.isEmpty()) {
      return 0;
    }

    return children.stream()
        .mapToInt(child -> 1 + findMaxDescendantDepth(child))
        .max()
        .orElse(0);
  }

  private void upsertItemTranslations(Long tenantId, ComponentItem item, NavbarItemRequest request) {
    if (request.translations() == null || request.translations().isEmpty())
      return;

    var supported = languageService.getSupportedLanguages(tenantId);
    for (var entry : request.translations().entrySet()) {
      Language lang = Language.fromCode(entry.getKey())
          .orElseThrow(() -> new IllegalArgumentException("language.invalid"));
      if (!supported.contains(lang)) {
        throw new IllegalArgumentException("language.unsupported: " + entry.getKey());
      }

      var payload = entry.getValue();
      var existingTranslation = itemTranslationRepository.findByItemIdAndLanguage(item.getId(), lang);

      ComponentItemTranslation tr;
      if (existingTranslation.isPresent()) {
        tr = existingTranslation.get();
      } else {
        tr = new ComponentItemTranslation();
        tr.setItem(item);
        tr.setLanguage(lang);
      }

      tr.setTitle(payload.title());
      tr.setSubtitle(payload.subtitle());
      tr.setUrl(payload.url());
      tr.setSeoTitle(payload.seoTitle());
      tr.setSeoDescription(payload.seoDescription());
      tr.setSeoKeywords(payload.seoKeywords());

      itemTranslationRepository.save(tr);
    }
  }
}
