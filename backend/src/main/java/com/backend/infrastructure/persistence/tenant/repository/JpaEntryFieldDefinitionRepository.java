package com.backend.infrastructure.persistence.tenant.repository;

import com.backend.domain.entity.EntryFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaEntryFieldDefinitionRepository extends JpaRepository<EntryFieldDefinition, Long> {
    List<EntryFieldDefinition> findByComponentTypeId(Long componentTypeId);
    Optional<EntryFieldDefinition> findByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey);
    boolean existsByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey);
}

