package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.ComponentEntry;

public interface JpaComponentEntryRepository extends JpaRepository<ComponentEntry, Long> {
    List<ComponentEntry> findByComponentId(Long componentId);

    List<ComponentEntry> findByComponentIdOrderBySortOrderAsc(Long componentId);

    List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrderAsc(Long componentId,
            com.backend.domain.enums.ComponentStatus status);

    boolean existsByUid(String uid);
}
