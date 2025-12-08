package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.SlotComponent;

interface SlotComponentJpaRepository extends JpaRepository<SlotComponent, Long> {

  List<SlotComponent> findBySlotIdOrderBySortOrder(Long slotId);

  List<SlotComponent> findBySlotIdIn(List<Long> slotIds);

  Optional<SlotComponent> findBySlotIdAndComponentId(Long slotId, Long componentId);

  boolean existsBySlotIdAndComponentId(Long slotId, Long componentId);

  @Query("SELECT MAX(sc.sortOrder) FROM SlotComponent sc WHERE sc.slotId = :slotId")
  Optional<Integer> findMaxSortOrderBySlotId(@Param("slotId") Long slotId);

  @Modifying
  @Query("DELETE FROM SlotComponent sc WHERE sc.slotId = :slotId")
  void deleteBySlotId(@Param("slotId") Long slotId);
}
