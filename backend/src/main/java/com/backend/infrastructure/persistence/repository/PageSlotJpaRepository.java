package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.PageSlot;

interface PageSlotJpaRepository extends JpaRepository<PageSlot, Long> {

  Optional<PageSlot> findByUid(String uid);

  List<PageSlot> findByPageIdOrderBySortOrder(Long pageId);

  Optional<PageSlot> findByPageIdAndSlotName(Long pageId, String slotName);

  @Query("SELECT ps FROM PageSlot ps WHERE ps.pageId IS NULL AND ps.isShared = true ORDER BY ps.sortOrder")
  List<PageSlot> findSharedSlots();

  boolean existsByPageIdAndSlotName(Long pageId, String slotName);

  @Query("SELECT ps FROM PageSlot ps WHERE (ps.pageId = :pageId OR (ps.pageId IS NULL AND ps.isShared = true)) AND ps.isActive = true ORDER BY ps.sortOrder")
  List<PageSlot> findActiveSlotsByPageIdIncludingShared(@Param("pageId") Long pageId);
}
