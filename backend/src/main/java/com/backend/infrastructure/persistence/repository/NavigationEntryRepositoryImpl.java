package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NavigationEntry;
import com.backend.domain.repository.NavigationEntryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NavigationEntryRepositoryImpl implements NavigationEntryRepository {

  private final NavigationEntryJpaRepository jpaRepository;

  @Override
  public Optional<NavigationEntry> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<NavigationEntry> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public List<NavigationEntry> findByNodeId(Long nodeId) {
    return jpaRepository.findByNodeIdOrderBySortOrderAscIdAsc(nodeId);
  }

  @Override
  public List<NavigationEntry> findByNodeIdIn(List<Long> nodeIds) {
    return jpaRepository.findByNodeIdInOrderByNodeIdAscSortOrderAscIdAsc(nodeIds);
  }

  @Override
  public NavigationEntry save(NavigationEntry entry) {
    return jpaRepository.save(entry);
  }

  @Override
  public List<NavigationEntry> saveAll(List<NavigationEntry> entries) {
    return jpaRepository.saveAll(entries);
  }

  @Override
  public void delete(NavigationEntry entry) {
    jpaRepository.delete(entry);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByUid(String uid) {
    return jpaRepository.existsByUid(uid);
  }

  @Override
  public int findMaxSortOrderByNodeId(Long nodeId) {
    Integer result = jpaRepository.findMaxSortOrderByNodeId(nodeId);
    return result != null ? result : -1;
  }
}

interface NavigationEntryJpaRepository extends JpaRepository<NavigationEntry, Long> {

  Optional<NavigationEntry> findByUid(String uid);

  List<NavigationEntry> findByNodeIdOrderBySortOrderAscIdAsc(Long nodeId);

  List<NavigationEntry> findByNodeIdInOrderByNodeIdAscSortOrderAscIdAsc(List<Long> nodeIds);

  boolean existsByUid(String uid);

  @Query("SELECT COALESCE(MAX(e.sortOrder), -1) FROM NavigationEntry e WHERE e.nodeId = :nodeId")
  Integer findMaxSortOrderByNodeId(@Param("nodeId") Long nodeId);
}
