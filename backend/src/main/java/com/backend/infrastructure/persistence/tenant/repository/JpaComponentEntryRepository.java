package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ComponentEntry;

public interface JpaComponentEntryRepository extends JpaRepository<ComponentEntry, Long> {
    List<ComponentEntry> findByIdIn(List<Long> ids);

    List<ComponentEntry> findByComponentId(Long componentId);

    List<ComponentEntry> findByComponentIdOrderBySortOrderAsc(Long componentId);

    List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrderAsc(Long componentId,
            com.backend.domain.enums.ComponentStatus status);

    boolean existsByUid(String uid);

    List<ComponentEntry> findByComponentIdInAndStatusOrderBySortOrderAsc(List<Long> componentIds,
            com.backend.domain.enums.ComponentStatus status);

    @Query("SELECT e FROM ComponentEntry e WHERE e.responsiveMedia.id = :responsiveMediaId")
    List<ComponentEntry> findByResponsiveMediaId(@Param("responsiveMediaId") Long responsiveMediaId);
}
