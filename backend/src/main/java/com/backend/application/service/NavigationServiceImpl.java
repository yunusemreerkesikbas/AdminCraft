package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse.EntryDeliveryDto;
import com.backend.application.dto.request.CreateEntryRequest;
import com.backend.application.dto.request.CreateNodeRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdateEntryRequest;
import com.backend.application.dto.request.UpdateNodeRequest;
import com.backend.application.dto.response.NavigationEntryResponse;
import com.backend.application.dto.response.NavigationNodeResponse;
import com.backend.domain.entity.NavigationEntry;
import com.backend.domain.entity.NavigationNode;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.NavigationEntryRepository;
import com.backend.domain.repository.NavigationNodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationServiceImpl implements NavigationService {

  private static final int MAX_DEPTH = 5;

  private final NavigationNodeRepository nodeRepository;
  private final NavigationEntryRepository entryRepository;

  // ==================== Node Operations ====================

  @Override
  @Transactional(readOnly = true)
  public List<NavigationNodeResponse> getRootNodes() {
    List<NavigationNode> roots = nodeRepository.findRootNodes();
    return roots.stream()
        .map(this::mapToNodeResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<NavigationNodeResponse> getNodeById(Long id) {
    List<NavigationNode> nodes = nodeRepository.findSubtreeByRootId(id);
    if (nodes.isEmpty()) {
      return Optional.empty();
    }

    List<Long> nodeIds = nodes.stream().map(NavigationNode::getId).toList();
    Map<Long, List<NavigationEntry>> entriesByNodeId = entryRepository.findByNodeIdIn(nodeIds).stream()
        .collect(Collectors.groupingBy(NavigationEntry::getNodeId));

    NavigationNodeResponse root = buildNodeTreeResponse(id, nodes, entriesByNodeId);
    return Optional.of(root);
  }

  @Override
  @Transactional
  public NavigationNodeResponse createRootNode(CreateNodeRequest request) {
    validateUidNotExists(request.uid());

    NavigationNode node = new NavigationNode();
    node.setUid(request.uid());
    node.setTitle(request.title());
    node.setPosition(request.position());
    node.setIsVisible(request.isVisible());
    node.setIsTab(request.isTab());
    node.setSortOrder(0);

    NavigationNode saved = nodeRepository.save(node);
    log.info("Created root navigation node: id={}, uid={}", saved.getId(), saved.getUid());

    return mapToNodeResponse(saved);
  }

  @Override
  @Transactional
  public NavigationNodeResponse addChildNode(Long parentId, CreateNodeRequest request) {
    NavigationNode parent = findNodeOrThrow(parentId);
    validateUidNotExists(request.uid());
    validateMaxDepth(parentId);

    int maxSortOrder = parent.getChildren().stream()
        .mapToInt(NavigationNode::getSortOrder)
        .max()
        .orElse(-1);

    NavigationNode child = new NavigationNode();
    child.setUid(request.uid());
    child.setTitle(request.title());
    child.setPosition(request.position());
    child.setIsVisible(request.isVisible());
    child.setIsTab(request.isTab());
    child.setParentId(parentId);
    child.setSortOrder(maxSortOrder + 1);

    NavigationNode saved = nodeRepository.save(child);
    log.info("Added child node: id={}, uid={}, parentId={}", saved.getId(), saved.getUid(), parentId);

    return mapToNodeResponse(saved);
  }

  @Override
  @Transactional
  public NavigationNodeResponse updateNode(Long id, UpdateNodeRequest request) {
    NavigationNode node = findNodeOrThrow(id);

    if (request.uid() != null && !request.uid().equals(node.getUid())) {
      validateUidNotExists(request.uid());
      node.setUid(request.uid());
    }

    if (request.parentId() != null && !request.parentId().equals(node.getParentId())) {
      validateParentChange(id, request.parentId());
      node.setParentId(request.parentId());
    }

    if (request.title() != null) {
      node.setTitle(request.title());
    }
    if (request.position() != null) {
      node.setPosition(request.position());
    }
    if (request.isVisible() != null) {
      node.setIsVisible(request.isVisible());
    }
    if (request.isTab() != null) {
      node.setIsTab(request.isTab());
    }

    NavigationNode saved = nodeRepository.save(node);
    log.info("Updated navigation node: id={}, uid={}", saved.getId(), saved.getUid());

    return mapToNodeResponse(saved);
  }

  @Override
  @Transactional
  public void deleteNode(Long id) {
    NavigationNode node = findNodeOrThrow(id);
    nodeRepository.delete(node);
    log.info("Deleted navigation node: id={}, uid={} (cascade to children and entries)", id, node.getUid());
  }

  @Override
  @Transactional
  public void reorderChildren(Long parentId, ReorderRequest<Long> request) {
    findNodeOrThrow(parentId);

    List<NavigationNode> children = nodeRepository.findByParentId(parentId);
    Map<Long, NavigationNode> childMap = children.stream()
        .collect(Collectors.toMap(NavigationNode::getId, Function.identity()));

    List<Long> items = request.items();
    validateReorderItems(items, childMap.keySet());

    List<NavigationNode> toUpdate = new ArrayList<>(items.size());
    IntStream.range(0, items.size()).forEach(i -> {
      Long childId = items.get(i);
      NavigationNode child = childMap.get(childId);
      child.setSortOrder(i);
      toUpdate.add(child);
    });

    nodeRepository.saveAll(toUpdate);
    log.info("Reordered {} children under parent node id={}", toUpdate.size(), parentId);
  }

  // ==================== Entry Operations ====================

  @Override
  @Transactional
  public NavigationEntryResponse createEntry(CreateEntryRequest request) {
    NavigationNode node = findNodeOrThrow(request.nodeId());
    validateEntryUidNotExists(request.uid());
    validateEntryData(request.itemType(), request.itemId(), request.url());

    int maxSortOrder = node.getEntries().stream()
        .mapToInt(NavigationEntry::getSortOrder)
        .max()
        .orElse(-1);

    NavigationEntry entry = new NavigationEntry();
    entry.setUid(request.uid());
    entry.setNodeId(request.nodeId());
    entry.setItemType(request.itemType());
    entry.setItemId(normalizeToNull(request.itemId()));
    entry.setUrl(normalizeToNull(request.url()));
    entry.setLinkName(request.linkName());
    entry.setLinkColor(request.linkColor());
    entry.setTarget(normalizeTarget(request.target()));
    entry.setIsExternal(request.isExternal());
    entry.setIsVisible(request.isVisible());
    entry.setSortOrder(maxSortOrder + 1);

    NavigationEntry saved = entryRepository.save(entry);
    log.info("Created navigation entry: id={}, uid={}, nodeId={}", saved.getId(), saved.getUid(), request.nodeId());

    return mapToEntryResponse(saved);
  }

  @Override
  @Transactional
  public NavigationEntryResponse updateEntry(Long id, UpdateEntryRequest request) {
    NavigationEntry entry = findEntryOrThrow(id);

    if (request.uid() != null && !request.uid().equals(entry.getUid())) {
      validateEntryUidNotExists(request.uid());
      entry.setUid(request.uid());
    }

    if (request.itemType() != null) {
      entry.setItemType(request.itemType());
    }
    if (request.itemId() != null) {
      entry.setItemId(normalizeToNull(request.itemId()));
    }
    if (request.url() != null) {
      entry.setUrl(normalizeToNull(request.url()));
    }
    if (request.linkName() != null) {
      entry.setLinkName(request.linkName());
    }
    if (request.linkColor() != null) {
      entry.setLinkColor(request.linkColor());
    }
    if (request.target() != null) {
      entry.setTarget(normalizeTarget(request.target()));
    }
    if (request.isExternal() != null) {
      entry.setIsExternal(request.isExternal());
    }
    if (request.isVisible() != null) {
      entry.setIsVisible(request.isVisible());
    }

    validateEntryData(entry.getItemType(), entry.getItemId(), entry.getUrl());

    NavigationEntry saved = entryRepository.save(entry);
    log.info("Updated navigation entry: id={}, uid={}", saved.getId(), saved.getUid());

    return mapToEntryResponse(saved);
  }

  @Override
  @Transactional
  public void deleteEntry(Long id) {
    NavigationEntry entry = findEntryOrThrow(id);
    entryRepository.delete(entry);
    log.info("Deleted navigation entry: id={}, uid={}", id, entry.getUid());
  }

  @Override
  @Transactional
  public void reorderEntries(Long nodeId, ReorderRequest<Long> request) {
    findNodeOrThrow(nodeId);

    List<NavigationEntry> entries = entryRepository.findByNodeId(nodeId);
    Map<Long, NavigationEntry> entryMap = entries.stream()
        .collect(Collectors.toMap(NavigationEntry::getId, Function.identity()));

    List<Long> items = request.items();
    validateReorderItems(items, entryMap.keySet());

    List<NavigationEntry> toUpdate = new ArrayList<>(items.size());
    IntStream.range(0, items.size()).forEach(i -> {
      Long entryId = items.get(i);
      NavigationEntry entry = entryMap.get(entryId);
      entry.setSortOrder(i);
      toUpdate.add(entry);
    });

    entryRepository.saveAll(toUpdate);
    log.info("Reordered {} entries under node id={}", toUpdate.size(), nodeId);
  }

  // ==================== CMS Delivery ====================

  @Override
  @Transactional(readOnly = true)
  public Optional<NavigationDeliveryResponse> getNavigationByUid(String uid) {
    List<NavigationNode> nodes = nodeRepository.findSubtreeByRootUid(uid);
    if (nodes.isEmpty()) {
      return Optional.empty();
    }

    Optional<NavigationNode> rootOpt = nodes.stream()
        .filter(n -> uid.equals(n.getUid()))
        .findFirst();

    if (rootOpt.isEmpty() || !Boolean.TRUE.equals(rootOpt.get().getIsVisible())) {
      return Optional.empty();
    }

    List<Long> nodeIds = nodes.stream().map(NavigationNode::getId).toList();
    Map<Long, List<NavigationEntry>> entriesByNodeId = entryRepository.findByNodeIdIn(nodeIds).stream()
        .collect(Collectors.groupingBy(NavigationEntry::getNodeId));

    Map<Long, List<NavigationNode>> childrenByParentId = nodes.stream()
        .filter(n -> n.getParentId() != null)
        .collect(Collectors.groupingBy(NavigationNode::getParentId));

    NavigationDeliveryResponse response = buildDeliveryResponse(rootOpt.get(), childrenByParentId, entriesByNodeId);
    return Optional.of(response);
  }

  // ==================== Private Helpers ====================

  private NavigationNode findNodeOrThrow(Long id) {
    return nodeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("NavigationNode", id));
  }

  private NavigationEntry findEntryOrThrow(Long id) {
    return entryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("NavigationEntry", id));
  }

  private void validateUidNotExists(String uid) {
    if (nodeRepository.existsByUid(uid)) {
      throw new IllegalArgumentException("navigation.node.uid.exists.simple");
    }
  }

  private void validateEntryUidNotExists(String uid) {
    if (entryRepository.existsByUid(uid)) {
      throw new IllegalArgumentException("navigation.entry.uid.exists.simple");
    }
  }

  private void validateMaxDepth(Long parentId) {
    int currentDepth = nodeRepository.calculateDepth(parentId);
    if (currentDepth >= MAX_DEPTH) {
      throw new IllegalArgumentException("navigation.depth.exceeded.simple");
    }
  }

  private void validateParentChange(Long nodeId, Long newParentId) {
    if (nodeId.equals(newParentId)) {
      throw new IllegalArgumentException("navigation.node.own.parent");
    }

    if (nodeRepository.isDescendantOf(newParentId, nodeId)) {
      throw new IllegalArgumentException("navigation.cycle.detected");
    }

    int newDepth = nodeRepository.calculateDepth(newParentId) + 1;
    if (newDepth > MAX_DEPTH) {
      throw new IllegalArgumentException("navigation.move.depth.exceeded.simple");
    }
  }

  private void validateEntryData(com.backend.domain.enums.NavigationItemType itemType, String itemId, String url) {
    if (itemType == null) {
      throw new IllegalArgumentException("navigation.item.type.required");
    }

    String normalizedItemId = normalizeToNull(itemId);
    String normalizedUrl = normalizeToNull(url);

    if (itemType.requiresUrl()) {
      if (normalizedUrl == null) {
        throw new IllegalArgumentException("navigation.url.required.simple");
      }
      if (normalizedItemId != null) {
        throw new IllegalArgumentException("navigation.url.itemId.must.be.null");
      }
      validateUrlAllowed(normalizedUrl);
      return;
    }

    if (itemType.requiresItemId()) {
      if (normalizedItemId == null) {
        throw new IllegalArgumentException("navigation.item.id.required.simple");
      }
      if (normalizedUrl != null) {
        throw new IllegalArgumentException("navigation.itemId.url.must.be.null");
      }
    }
  }

  private void validateReorderItems(List<Long> items, Set<Long> existingIds) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("navigation.reorder.items.required");
    }
    if (new HashSet<>(items).size() != items.size()) {
      throw new IllegalArgumentException("navigation.reorder.duplicate");
    }
    if (items.size() != existingIds.size()) {
      throw new IllegalArgumentException("navigation.reorder.size.mismatch");
    }
    if (!new HashSet<>(items).equals(existingIds)) {
      throw new IllegalArgumentException("navigation.reorder.invalid.items");
    }
  }

  private String normalizeToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeTarget(String target) {
    String normalized = normalizeToNull(target);
    return normalized == null ? "_self" : normalized;
  }

  private void validateUrlAllowed(String url) {
    String trimmed = url.trim();
    if (trimmed.startsWith("/")) {
      return;
    }
    URI uri;
    try {
      uri = URI.create(trimmed);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("navigation.url.invalid");
    }
    String scheme = uri.getScheme();
    if (scheme == null) {
      throw new IllegalArgumentException("navigation.url.invalid");
    }
    String normalized = scheme.toLowerCase();
    if (!"http".equals(normalized) && !"https".equals(normalized)) {
      throw new IllegalArgumentException("navigation.url.scheme.invalid");
    }
  }

  // ==================== Mappers ====================

  private NavigationNodeResponse mapToNodeResponse(NavigationNode node) {
    return NavigationNodeResponse.builder()
        .id(node.getId())
        .uuid(node.getUuid())
        .uid(node.getUid())
        .title(node.getTitle())
        .parentId(node.getParentId())
        .position(node.getPosition())
        .sortOrder(node.getSortOrder())
        .isVisible(node.getIsVisible())
        .isTab(node.getIsTab())
        .entries(new ArrayList<>())
        .children(new ArrayList<>())
        .build();
  }

  private NavigationNodeResponse buildNodeTreeResponse(Long rootId,
      List<NavigationNode> nodes,
      Map<Long, List<NavigationEntry>> entriesByNodeId) {

    Map<Long, NavigationNodeResponse> responseMap = nodes.stream()
        .map(this::mapToNodeResponse)
        .collect(Collectors.toMap(NavigationNodeResponse::getId, Function.identity()));

    nodes.forEach(node -> {
      Long parentId = node.getParentId();
      if (parentId != null) {
        NavigationNodeResponse parent = responseMap.get(parentId);
        NavigationNodeResponse child = responseMap.get(node.getId());
        if (parent != null && child != null) {
          parent.getChildren().add(child);
        }
      }
    });

    responseMap.values().forEach(r -> {
      List<NavigationEntry> entries = entriesByNodeId.getOrDefault(r.getId(), List.of());
      r.setEntries(entries.stream().map(this::mapToEntryResponse).toList());
    });

    NavigationNodeResponse root = responseMap.get(rootId);
    sortNodeTree(root);
    return root;
  }

  private void sortNodeTree(NavigationNodeResponse node) {
    if (node == null) {
      return;
    }
    if (node.getChildren() != null) {
      node.getChildren().sort(Comparator
          .comparing(NavigationNodeResponse::getSortOrder, Comparator.nullsLast(Integer::compareTo))
          .thenComparing(NavigationNodeResponse::getId, Comparator.nullsLast(Long::compareTo)));
      node.getChildren().forEach(this::sortNodeTree);
    }
  }

  private NavigationDeliveryResponse buildDeliveryResponse(
      NavigationNode root,
      Map<Long, List<NavigationNode>> childrenByParentId,
      Map<Long, List<NavigationEntry>> entriesByNodeId) {

    List<EntryDeliveryDto> entries = entriesByNodeId.getOrDefault(root.getId(), List.of()).stream()
        .filter(e -> Boolean.TRUE.equals(e.getIsVisible()))
        .map(this::mapToEntryDeliveryDto)
        .toList();

    List<NavigationDeliveryResponse> children = childrenByParentId.getOrDefault(root.getId(), List.of()).stream()
        .filter(c -> Boolean.TRUE.equals(c.getIsVisible()))
        .sorted(Comparator
            .comparing(NavigationNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(NavigationNode::getId, Comparator.nullsLast(Long::compareTo)))
        .map(child -> buildDeliveryResponse(child, childrenByParentId, entriesByNodeId))
        .toList();

    return NavigationDeliveryResponse.builder()
        .uid(root.getUid())
        .title(root.getTitle())
        .position(root.getPosition())
        .isTab(root.getIsTab())
        .entries(entries)
        .children(children)
        .build();
  }

  private NavigationEntryResponse mapToEntryResponse(NavigationEntry entry) {
    return NavigationEntryResponse.builder()
        .id(entry.getId())
        .uuid(entry.getUuid())
        .uid(entry.getUid())
        .nodeId(entry.getNodeId())
        .itemType(entry.getItemType())
        .itemId(entry.getItemId())
        .url(entry.getUrl())
        .linkName(entry.getLinkName())
        .linkColor(entry.getLinkColor())
        .target(entry.getTarget())
        .isExternal(entry.getIsExternal())
        .sortOrder(entry.getSortOrder())
        .isVisible(entry.getIsVisible())
        .build();
  }

  private EntryDeliveryDto mapToEntryDeliveryDto(NavigationEntry entry) {
    return EntryDeliveryDto.builder()
        .uid(entry.getUid())
        .itemType(entry.getItemType())
        .itemId(entry.getItemId())
        .url(entry.getUrl())
        .linkName(entry.getLinkName())
        .linkColor(entry.getLinkColor())
        .target(entry.getTarget())
        .isExternal(entry.getIsExternal())
        .build();
  }
}
