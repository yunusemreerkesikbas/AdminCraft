package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ComponentEntryJpaRepository extends JpaRepository<ComponentEntry, Long> {

  List<ComponentEntry> findByIdIn(List<Long> ids);

  @Query("SELECT e FROM ComponentEntry e WHERE e.componentId = :componentId ORDER BY e.sortOrder ASC")
  List<ComponentEntry> findByComponentIdOrderBySortOrder(@Param("componentId") Long componentId);

  List<ComponentEntry> findByComponentIdOrderBySortOrderAsc(Long componentId);

  List<ComponentEntry> findByComponentId(Long componentId);

  List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrderAsc(
      Long componentId,
      com.backend.domain.enums.ComponentStatus status);

  List<ComponentEntry> findByComponentIdInAndStatusOrderBySortOrderAsc(
      List<Long> componentIds,
      com.backend.domain.enums.ComponentStatus status);

  @Query("SELECT e FROM ComponentEntry e WHERE e.responsiveMedia.id = :responsiveMediaId")
  List<ComponentEntry> findByResponsiveMediaId(@Param("responsiveMediaId") Long responsiveMediaId);

  boolean existsByUid(String uid);

  void deleteByComponentId(Long componentId);
}
