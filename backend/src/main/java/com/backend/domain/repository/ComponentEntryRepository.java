package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ComponentEntry;

public interface ComponentEntryRepository {
    ComponentEntry save(ComponentEntry entry);

    Optional<ComponentEntry> findById(Long id);

    List<ComponentEntry> findByIdIn(List<Long> ids);

    List<ComponentEntry> findByComponentId(Long componentId);

    List<ComponentEntry> findByComponentIdOrderBySortOrder(Long componentId);

    List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrder(Long componentId,
            com.backend.domain.enums.ComponentStatus status);

    void delete(ComponentEntry entry);

    boolean existsByUid(String uid);

    List<ComponentEntry> findByComponentIdInAndStatusOrderBySortOrder(List<Long> componentIds,
            com.backend.domain.enums.ComponentStatus status);

    /** Preview-aware variant: matches any status in the supplied set. */
    List<ComponentEntry> findByComponentIdInAndStatusInOrderBySortOrder(List<Long> componentIds,
            java.util.Collection<com.backend.domain.enums.ComponentStatus> statuses);

    List<ComponentEntry> findByResponsiveMediaId(Long responsiveMediaId);
}
