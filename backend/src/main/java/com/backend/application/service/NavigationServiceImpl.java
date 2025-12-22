package com.backend.application.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse.EntryDeliveryDto;
import com.backend.application.dto.request.CreateEntryCompositeRequest;
import com.backend.application.dto.request.CreateEntryRequest;
import com.backend.application.dto.request.CreateNodeCompositeRequest;
import com.backend.application.dto.request.CreateNodeRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdateEntryCompositeRequest;
import com.backend.application.dto.request.UpdateEntryRequest;
import com.backend.application.dto.request.UpdateNodeCompositeRequest;
import com.backend.application.dto.request.UpdateNodeRequest;
import com.backend.application.dto.response.NavigationEntryCompositeResponse;
import com.backend.application.dto.response.NavigationEntryResponse;
import com.backend.application.dto.response.NavigationNodeCompositeResponse;
import com.backend.application.dto.response.NavigationNodeResponse;
import com.backend.domain.entity.NavigationEntry;
import com.backend.domain.entity.NavigationEntryI18n;
import com.backend.domain.entity.NavigationNode;
import com.backend.domain.entity.NavigationNodeI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.NavigationEntryI18nRepository;
import com.backend.domain.repository.NavigationEntryRepository;
import com.backend.domain.repository.NavigationNodeI18nRepository;
import com.backend.domain.repository.NavigationNodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationServiceImpl implements NavigationService {

  private static final int MAX_DEPTH = 5;
  private static final Language DEFAULT_LANGUAGE = Language.TR;

  private final NavigationNodeRepository nodeRepository;
  private final NavigationEntryRepository entryRepository;
  private final NavigationNodeI18nRepository nodeI18nRepository;
  private final NavigationEntryI18nRepository entryI18nRepository;

  /**
   * Retrieve top-level navigation nodes mapped to response DTOs including localized titles.
   *
   * @return a list of NavigationNodeResponse representing root nodes with their title in the default language
   */
  @Override
  @Transactional(readOnly = true)
  public List<NavigationNodeResponse> getRootNodes() {
    List<NavigationNode> roots = nodeRepository.findRootNodes();
    return roots.stream()
        .map(this::mapToNodeResponseWithI18n)
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
    node.setPosition(request.position());
    node.setIsVisible(request.isVisible());
    node.setIsTab(request.isTab());
    node.setSortOrder(0);

    NavigationNode saved = nodeRepository.save(node);
    if (request.title() != null) {
      NavigationNodeI18n i18n = new NavigationNodeI18n(saved.getId(), getDefaultLanguage(), request.title());
      nodeI18nRepository.save(i18n);
    }

    log.info("Created root navigation node: id={}, uid={}", saved.getId(), saved.getUid());

    return mapToNodeResponseWithI18n(saved);
  }

  @Override
  @Transactional
  public NavigationNodeResponse addChildNode(Long parentId, CreateNodeRequest request) {
    findNodeOrThrow(parentId);
    validateUidNotExists(request.uid());
    validateMaxDepth(parentId);

    int maxSortOrder = nodeRepository.findMaxChildSortOrderByParentId(parentId);

    NavigationNode child = new NavigationNode();
    child.setUid(request.uid());
    child.setPosition(request.position());
    child.setIsVisible(request.isVisible());
    child.setIsTab(request.isTab());
    child.setParentId(parentId);
    child.setSortOrder(maxSortOrder + 1);

    NavigationNode saved = nodeRepository.save(child);

    if (request.title() != null) {
      NavigationNodeI18n i18n = new NavigationNodeI18n(saved.getId(), getDefaultLanguage(), request.title());
      nodeI18nRepository.save(i18n);
    }

    log.info("Added child node: id={}, uid={}, parentId={}", saved.getId(), saved.getUid(), parentId);

    return mapToNodeResponseWithI18n(saved);
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

    // Update i18n title
    if (request.title() != null) {
      updateNodeI18nTitle(id, request.title());
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

    return mapToNodeResponseWithI18n(saved);
  }

  private void updateNodeI18nTitle(Long nodeId, String title) {
    Language lang = getDefaultLanguage();
    NavigationNodeI18n i18n = nodeI18nRepository.findByNodeIdAndLanguage(nodeId, lang)
        .orElseGet(() -> {
          NavigationNodeI18n newI18n = new NavigationNodeI18n();
          newI18n.setNodeId(nodeId);
          newI18n.setLanguage(lang);
          return newI18n;
        });
    i18n.setTitle(title);
    nodeI18nRepository.save(i18n);
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
    findNodeOrThrow(request.nodeId());
    validateEntryUidNotExists(request.uid());
    validateEntryData(request.itemType(), request.itemId(), request.url());

    int maxSortOrder = entryRepository.findMaxSortOrderByNodeId(request.nodeId());

    NavigationEntry entry = new NavigationEntry();
    entry.setUid(request.uid());
    entry.setNodeId(request.nodeId());
    entry.setItemType(request.itemType());
    entry.setItemId(normalizeToNull(request.itemId()));
    entry.setUrl(normalizeToNull(request.url()));
    entry.setLinkColor(request.linkColor());
    entry.setTarget(normalizeTarget(request.target()));
    entry.setIsExternal(request.isExternal());
    entry.setIsVisible(request.isVisible());
    entry.setSortOrder(maxSortOrder + 1);

    NavigationEntry saved = entryRepository.save(entry);

    if (request.linkName() != null) {
      NavigationEntryI18n i18n = new NavigationEntryI18n(saved.getId(), getDefaultLanguage(), request.linkName());
      entryI18nRepository.save(i18n);
    }

    log.info("Created navigation entry: id={}, uid={}, nodeId={}", saved.getId(), saved.getUid(), request.nodeId());

    return mapToEntryResponseWithI18n(saved);
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
      updateEntryI18nLinkName(id, request.linkName());
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

    return mapToEntryResponseWithI18n(saved);
  }

  private void updateEntryI18nLinkName(Long entryId, String linkName) {
    Language lang = getDefaultLanguage();
    NavigationEntryI18n i18n = entryI18nRepository.findByEntryIdAndLanguage(entryId, lang)
        .orElseGet(() -> {
          NavigationEntryI18n newI18n = new NavigationEntryI18n();
          newI18n.setEntryId(entryId);
          newI18n.setLanguage(lang);
          return newI18n;
        });
    i18n.setLinkName(linkName);
    entryI18nRepository.save(i18n);
  }

  @Override
  @Transactional
  public void deleteEntry(Long id) {
    NavigationEntry entry = findEntryOrThrow(id);
    entryRepository.delete(entry);
    log.info("Deleted navigation entry: id={}, uid={}", id, entry.getUid());
  }

  /**
   * Reorders the entries of a node to match the sequence of entry IDs in the request.
   *
   * Updates each entry's sortOrder to the index of its ID in request.items() and persists the changes.
   *
   * @param nodeId the ID of the node whose entries will be reordered
   * @param request a reorder request containing the ordered list of entry IDs
   */
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

  /**
   * Create a new navigation node and persist its translations.
   *
   * <p>Validates UID uniqueness and, if a parentId is provided, validates the parent exists
   * and that the resulting depth does not exceed the configured maximum. Computes the node's
   * sort order under the parent (if any), saves the node, and persists one I18n record per
   * language from the request's translations map.</p>
   *
   * @param request composite request containing node properties and a map of translations
   * @return a composite response containing the persisted node and its saved translations
   */
  @Override
  @Transactional
  public NavigationNodeCompositeResponse createNodeComposite(CreateNodeCompositeRequest request) {
    validateUidNotExists(request.uid());
    if (request.parentId() != null) {
      findNodeOrThrow(request.parentId());
      validateMaxDepth(request.parentId());
    }

    int sortOrder = 0;
    if (request.parentId() != null) {
      sortOrder = nodeRepository.findMaxChildSortOrderByParentId(request.parentId()) + 1;
    }

    NavigationNode node = new NavigationNode();
    node.setUid(request.uid());
    node.setPosition(request.position());
    node.setIsVisible(request.isVisible());
    node.setIsTab(request.isTab());
    node.setParentId(request.parentId());
    node.setSortOrder(sortOrder);

    NavigationNode savedNode = nodeRepository.save(node);

    List<NavigationNodeI18n> i18nList = new ArrayList<>();
    for (var entry : request.translations().entrySet()) {
      NavigationNodeI18n i18n = new NavigationNodeI18n();
      i18n.setNodeId(savedNode.getId());
      i18n.setLanguage(entry.getKey());
      if (entry.getValue() != null && entry.getValue().title() != null) {
        i18n.setTitle(entry.getValue().title());
      }
      i18nList.add(nodeI18nRepository.save(i18n));
    }

    log.info("Created navigation node with {} translations: id={}, uid={}",
        i18nList.size(), savedNode.getId(), savedNode.getUid());

    return NavigationNodeCompositeResponse.from(savedNode, i18nList);
  }

  /**
   * Update a navigation node's mutable fields and upsert its translations.
   *
   * Applies changes to position, visibility, and tab flag when present in the request,
   * and creates or updates the node's i18n records for each provided language.
   *
   * @param id      the identifier of the navigation node to update
   * @param request the update request containing optional node fields and a map of translations by language
   * @return        a composite response containing the updated NavigationNode and the list of saved translations
   */
  @Override
  @Transactional
  public NavigationNodeCompositeResponse updateNodeComposite(Long id, UpdateNodeCompositeRequest request) {
    NavigationNode node = findNodeOrThrow(id);

    if (request.position() != null) {
      node.setPosition(request.position());
    }
    if (request.isVisible() != null) {
      node.setIsVisible(request.isVisible());
    }
    if (request.isTab() != null) {
      node.setIsTab(request.isTab());
    }

    NavigationNode savedNode = nodeRepository.save(node);

    List<NavigationNodeI18n> i18nList = new ArrayList<>();
    for (var entry : request.translations().entrySet()) {
      NavigationNodeI18n i18n = nodeI18nRepository.findByNodeIdAndLanguage(id, entry.getKey())
          .orElseGet(() -> {
            NavigationNodeI18n newI18n = new NavigationNodeI18n();
            newI18n.setNodeId(id);
            newI18n.setLanguage(entry.getKey());
            return newI18n;
          });

      if (entry.getValue() != null && entry.getValue().title() != null) {
        i18n.setTitle(entry.getValue().title());
      }
      i18nList.add(nodeI18nRepository.save(i18n));
    }

    log.info("Updated navigation node with {} translations: id={}, uid={}",
        i18nList.size(), savedNode.getId(), savedNode.getUid());

    return NavigationNodeCompositeResponse.from(savedNode, i18nList);
  }

  /**
   * Creates a navigation entry under the specified node and persists its translations.
   *
   * Validates the target node, UID uniqueness, and entry data; sets sort order to follow existing entries;
   * saves the entry and each provided translation record, then returns a composite response containing
   * the persisted entry and its translations.
   *
   * @param request the request containing entry fields, target node id, and translations by language
   * @return a NavigationEntryCompositeResponse containing the saved entry and its persisted translations
   */
  @Override
  @Transactional
  public NavigationEntryCompositeResponse createEntryComposite(CreateEntryCompositeRequest request) {
    findNodeOrThrow(request.nodeId());
    validateEntryUidNotExists(request.uid());
    validateEntryData(request.itemType(), request.itemId(), request.url());
    int maxSortOrder = entryRepository.findMaxSortOrderByNodeId(request.nodeId());
    NavigationEntry entry = new NavigationEntry();
    entry.setUid(request.uid());
    entry.setNodeId(request.nodeId());
    entry.setItemType(request.itemType());
    entry.setItemId(normalizeToNull(request.itemId()));
    entry.setUrl(normalizeToNull(request.url()));
    entry.setLinkColor(request.linkColor());
    entry.setTarget(normalizeTarget(request.target()));
    entry.setIsExternal(request.isExternal());
    entry.setIsVisible(request.isVisible());
    entry.setSortOrder(maxSortOrder + 1);
    NavigationEntry savedEntry = entryRepository.save(entry);
    List<NavigationEntryI18n> i18nList = new ArrayList<>();
    for (var translationEntry : request.translations().entrySet()) {
      NavigationEntryI18n i18n = new NavigationEntryI18n();
      i18n.setEntryId(savedEntry.getId());
      i18n.setLanguage(translationEntry.getKey());
      if (translationEntry.getValue() != null && translationEntry.getValue().linkName() != null) {
        i18n.setLinkName(translationEntry.getValue().linkName());
      }
      i18nList.add(entryI18nRepository.save(i18n));
    }
    log.info("Created navigation entry with {} translations: id={}, uid={}, nodeId={}",
        i18nList.size(), savedEntry.getId(), savedEntry.getUid(), request.nodeId());

    return NavigationEntryCompositeResponse.from(savedEntry, i18nList);
  }

  /**
   * Updates fields of an existing navigation entry and upserts its translations.
   *
   * Applies any non-null values from the request to the entry, validates entry data,
   * persists the entry, upserts each provided translation (creating missing i18n records),
   * and returns the saved entry together with all persisted translation records.
   *
   * @param id the id of the navigation entry to update
   * @param request the update request containing fields to change and language-keyed translations
   * @return the updated entry and its persisted translations as a composite response
   */
  @Override
  @Transactional
  public NavigationEntryCompositeResponse updateEntryComposite(Long id, UpdateEntryCompositeRequest request) {
    NavigationEntry entry = findEntryOrThrow(id);
    if (request.itemType() != null) {
      entry.setItemType(request.itemType());
    }
    if (request.itemId() != null) {
      entry.setItemId(normalizeToNull(request.itemId()));
    }
    if (request.url() != null) {
      entry.setUrl(normalizeToNull(request.url()));
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

    NavigationEntry savedEntry = entryRepository.save(entry);
    List<NavigationEntryI18n> i18nList = new ArrayList<>();
    for (var translationEntry : request.translations().entrySet()) {
      NavigationEntryI18n i18n = entryI18nRepository.findByEntryIdAndLanguage(id, translationEntry.getKey())
          .orElseGet(() -> {
            NavigationEntryI18n newI18n = new NavigationEntryI18n();
            newI18n.setEntryId(id);
            newI18n.setLanguage(translationEntry.getKey());
            return newI18n;
          });

      if (translationEntry.getValue() != null && translationEntry.getValue().linkName() != null) {
        i18n.setLinkName(translationEntry.getValue().linkName());
      }
      i18nList.add(entryI18nRepository.save(i18n));
    }
    log.info("Updated navigation entry with {} translations: id={}, uid={}",
        i18nList.size(), savedEntry.getId(), savedEntry.getUid());

    return NavigationEntryCompositeResponse.from(savedEntry, i18nList);
  }

  /**
   * Builds a delivery DTO for a navigation tree identified by the given root UID.
   *
   * <p>Locates the subtree for the specified root UID and, if the root exists and is visible,
   * returns a delivery representation containing the root, its visible descendants, and their
   * visible entries organized hierarchically.</p>
   *
   * @param uid the UID of the root navigation node to retrieve
   * @return an Optional containing the assembled NavigationDeliveryResponse when the root exists and is visible, or an empty Optional otherwise
   */

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

  private Language getDefaultLanguage() {
    // TODO: In future, can be fetched from tenant settings
    return DEFAULT_LANGUAGE;
  }

  private NavigationNodeResponse mapToNodeResponseWithI18n(NavigationNode node) {
    String title = nodeI18nRepository.findByNodeIdAndLanguage(node.getId(), getDefaultLanguage())
        .map(NavigationNodeI18n::getTitle)
        .orElse(null);
    return buildNodeResponse(node, title);
  }

  private NavigationNodeResponse buildNodeResponse(NavigationNode node, String title) {
    return NavigationNodeResponse.builder()
        .id(node.getId())
        .uuid(node.getUuid())
        .uid(node.getUid())
        .title(title)
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

    List<Long> nodeIds = nodes.stream().map(NavigationNode::getId).toList();
    Map<Long, String> titlesByNodeId = nodeI18nRepository.findByNodeIdIn(nodeIds).stream()
        .filter(i18n -> i18n.getLanguage() == getDefaultLanguage())
        .collect(Collectors.toMap(NavigationNodeI18n::getNodeId, NavigationNodeI18n::getTitle, (a, b) -> a));

    List<Long> entryIds = entriesByNodeId.values().stream()
        .flatMap(List::stream)
        .map(NavigationEntry::getId)
        .toList();
    Map<Long, String> linkNamesByEntryId = entryI18nRepository.findByEntryIdIn(entryIds).stream()
        .filter(i18n -> i18n.getLanguage() == getDefaultLanguage())
        .collect(Collectors.toMap(NavigationEntryI18n::getEntryId, NavigationEntryI18n::getLinkName, (a, b) -> a));

    Map<Long, NavigationNodeResponse> responseMap = nodes.stream()
        .map(n -> buildNodeResponse(n, titlesByNodeId.get(n.getId())))
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
      r.setEntries(entries.stream()
          .map(e -> buildEntryResponse(e, linkNamesByEntryId.get(e.getId())))
          .toList());
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

    String title = nodeI18nRepository.findByNodeIdAndLanguage(root.getId(), getDefaultLanguage())
        .map(NavigationNodeI18n::getTitle)
        .orElse(null);
    List<NavigationEntry> nodeEntries = entriesByNodeId.getOrDefault(root.getId(), List.of());
    List<Long> entryIds = nodeEntries.stream().map(NavigationEntry::getId).toList();
    Map<Long, String> linkNamesByEntryId = entryI18nRepository.findByEntryIdIn(entryIds).stream()
        .filter(i18n -> i18n.getLanguage() == getDefaultLanguage())
        .collect(Collectors.toMap(NavigationEntryI18n::getEntryId, NavigationEntryI18n::getLinkName, (a, b) -> a));

    List<EntryDeliveryDto> entries = nodeEntries.stream()
        .filter(e -> Boolean.TRUE.equals(e.getIsVisible()))
        .map(e -> buildEntryDeliveryDto(e, linkNamesByEntryId.get(e.getId())))
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
        .title(title)
        .position(root.getPosition())
        .isTab(root.getIsTab())
        .entries(entries)
        .children(children)
        .build();
  }

  private NavigationEntryResponse mapToEntryResponseWithI18n(NavigationEntry entry) {
    String linkName = entryI18nRepository.findByEntryIdAndLanguage(entry.getId(), getDefaultLanguage())
        .map(NavigationEntryI18n::getLinkName)
        .orElse(null);
    return buildEntryResponse(entry, linkName);
  }

  private NavigationEntryResponse buildEntryResponse(NavigationEntry entry, String linkName) {
    return NavigationEntryResponse.builder()
        .id(entry.getId())
        .uuid(entry.getUuid())
        .uid(entry.getUid())
        .nodeId(entry.getNodeId())
        .itemType(entry.getItemType())
        .itemId(entry.getItemId())
        .url(entry.getUrl())
        .linkName(linkName)
        .linkColor(entry.getLinkColor())
        .target(entry.getTarget())
        .isExternal(entry.getIsExternal())
        .sortOrder(entry.getSortOrder())
        .isVisible(entry.getIsVisible())
        .build();
  }

  private EntryDeliveryDto buildEntryDeliveryDto(NavigationEntry entry, String linkName) {
    return EntryDeliveryDto.builder()
        .uid(entry.getUid())
        .itemType(entry.getItemType())
        .itemId(entry.getItemId())
        .url(entry.getUrl())
        .linkName(linkName)
        .linkColor(entry.getLinkColor())
        .target(entry.getTarget())
        .isExternal(entry.getIsExternal())
        .build();
  }
}