package com.backend.domain.repository;

import com.backend.domain.entity.ComponentEntry;
import java.util.List;
import java.util.Optional;

public interface ComponentEntryRepository {
    ComponentEntry save(ComponentEntry entry);
    Optional<ComponentEntry> findById(Long id);
    List<ComponentEntry> findByComponentId(Long componentId);
    List<ComponentEntry> findByComponentIdOrderBySortOrder(Long componentId);
    void delete(ComponentEntry entry);
    boolean existsByUid(String uid);
}



