package com.backend.domain.repository;

import com.backend.domain.entity.ComponentItem;

import java.util.List;
import java.util.Optional;

public interface ComponentItemRepository {
  List<ComponentItem> findRootsByComponentId(Long componentId);

  List<ComponentItem> findAllByComponentId(Long componentId);

  boolean existsByComponentIdAndUid(Long componentId, String uid);

  Optional<ComponentItem> findByIdAndComponentId(Long id, Long componentId);

  List<ComponentItem> findByParentId(Long parentId);

  ComponentItem save(ComponentItem item);

  void delete(ComponentItem item);
}

