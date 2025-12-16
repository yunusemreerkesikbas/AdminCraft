package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.NavigationNode;

public interface NavigationNodeRepository {

  List<NavigationNode> findRootNodes();

  Optional<NavigationNode> findById(Long id);

  Optional<NavigationNode> findByIdWithChildrenAndEntries(Long id);

  Optional<NavigationNode> findByUid(String uid);

  Optional<NavigationNode> findByUidWithChildrenAndEntries(String uid);

  List<NavigationNode> findSubtreeByRootId(Long rootId);

  List<NavigationNode> findSubtreeByRootUid(String uid);

  List<NavigationNode> findByParentId(Long parentId);

  NavigationNode save(NavigationNode node);

  List<NavigationNode> saveAll(List<NavigationNode> nodes);

  void delete(NavigationNode node);

  void deleteById(Long id);

  boolean existsByUid(String uid);

  int calculateDepth(Long nodeId);

  boolean isDescendantOf(Long nodeId, Long potentialAncestorId);
}
