package com.backend.infrastructure.persistence.tenant.repository;

import com.backend.domain.entity.ComponentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaComponentEntryRepository extends JpaRepository<ComponentEntry, Long> {
    List<ComponentEntry> findByComponentId(Long componentId);
    List<ComponentEntry> findByComponentIdOrderBySortOrderAsc(Long componentId);
    boolean existsByUid(String uid);
}

