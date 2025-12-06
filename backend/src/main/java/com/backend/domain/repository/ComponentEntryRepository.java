package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ComponentEntry;

public interface ComponentEntryRepository {
    ComponentEntry save(ComponentEntry entry);

    Optional<ComponentEntry> findById(Long id);

    List<ComponentEntry> findByComponentId(Long componentId);

    List<ComponentEntry> findByComponentIdOrderBySortOrder(Long componentId);

    List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrder(Long componentId,
            com.backend.domain.enums.ComponentStatus status);

    void delete(ComponentEntry entry);

    boolean existsByUid(String uid);
}
