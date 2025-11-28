package com.backend.domain.repository;

import com.backend.domain.entity.EntryFieldDefinition;
import java.util.List;
import java.util.Optional;

public interface EntryFieldDefinitionRepository {
    EntryFieldDefinition save(EntryFieldDefinition definition);
    Optional<EntryFieldDefinition> findById(Long id);
    List<EntryFieldDefinition> findByComponentTypeId(Long componentTypeId);
    Optional<EntryFieldDefinition> findByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey);
    boolean existsByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey);
    void delete(EntryFieldDefinition definition);
}



