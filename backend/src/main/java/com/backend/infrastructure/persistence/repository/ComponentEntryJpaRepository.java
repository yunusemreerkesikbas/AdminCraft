package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ComponentEntryJpaRepository extends JpaRepository<ComponentEntry, Long> {

  @Query("SELECT e FROM ComponentEntry e WHERE e.componentId = :componentId ORDER BY e.sortOrder ASC")
  List<ComponentEntry> findByComponentIdOrderBySortOrder(@Param("componentId") Long componentId);

  List<ComponentEntry> findByComponentId(Long componentId);

  void deleteByComponentId(Long componentId);
}
