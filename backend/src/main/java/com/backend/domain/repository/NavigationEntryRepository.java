package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.NavigationEntry;

public interface NavigationEntryRepository {

  Optional<NavigationEntry> findById(Long id);

  Optional<NavigationEntry> findByUid(String uid);

  List<NavigationEntry> findByNodeId(Long nodeId);

  List<NavigationEntry> findByNodeIdIn(List<Long> nodeIds);

  NavigationEntry save(NavigationEntry entry);

  List<NavigationEntry> saveAll(List<NavigationEntry> entries);

  void delete(NavigationEntry entry);

  void deleteById(Long id);

  boolean existsByUid(String uid);

  int findMaxSortOrderByNodeId(Long nodeId);
}
