package com.backend.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.enums.ComponentStatus;

public interface ComponentEntryRepository {
    ComponentEntry save(ComponentEntry entry);

    Optional<ComponentEntry> findById(Long id);

    List<ComponentEntry> findByIdIn(List<Long> ids);

    List<ComponentEntry> findByComponentId(Long componentId);

    List<ComponentEntry> findByComponentIdOrderBySortOrder(Long componentId);

    List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrder(Long componentId,
            ComponentStatus status);

    void delete(ComponentEntry entry);

    boolean existsByUid(String uid);

    List<ComponentEntry> findByComponentIdInAndStatusOrderBySortOrder(List<Long> componentIds,
            ComponentStatus status);

    /** Preview-aware variant: matches any status in the supplied set. */
    List<ComponentEntry> findByComponentIdInAndStatusInOrderBySortOrder(List<Long> componentIds,
            Collection<ComponentStatus> statuses);

    List<ComponentEntry> findByResponsiveMediaId(Long responsiveMediaId);
}
