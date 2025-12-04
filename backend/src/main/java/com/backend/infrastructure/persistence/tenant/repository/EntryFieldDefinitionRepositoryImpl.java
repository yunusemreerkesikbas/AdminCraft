package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.repository.EntryFieldDefinitionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EntryFieldDefinitionRepositoryImpl implements EntryFieldDefinitionRepository {

    private final JpaEntryFieldDefinitionRepository jpaRepository;

    @Override
    public EntryFieldDefinition save(EntryFieldDefinition definition) {
        return jpaRepository.save(definition);
    }

    @Override
    public Optional<EntryFieldDefinition> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<EntryFieldDefinition> findByComponentTypeId(Long componentTypeId) {
        return jpaRepository.findByComponentTypeId(componentTypeId);
    }

    @Override
    public Optional<EntryFieldDefinition> findByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey) {
        return jpaRepository.findByComponentTypeIdAndFieldKey(componentTypeId, fieldKey);
    }

    @Override
    public boolean existsByComponentTypeIdAndFieldKey(Long componentTypeId, String fieldKey) {
        return jpaRepository.existsByComponentTypeIdAndFieldKey(componentTypeId, fieldKey);
    }

    @Override
    public void delete(EntryFieldDefinition definition) {
        jpaRepository.delete(definition);
    }
}
