package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.repository.ComponentEntryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ComponentEntryRepositoryImpl implements ComponentEntryRepository {

    private final JpaComponentEntryRepository jpaRepository;

    @Override
    public ComponentEntry save(ComponentEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<ComponentEntry> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ComponentEntry> findByComponentId(Long componentId) {
        return jpaRepository.findByComponentId(componentId);
    }

    @Override
    public List<ComponentEntry> findByComponentIdOrderBySortOrder(Long componentId) {
        return jpaRepository.findByComponentIdOrderBySortOrderAsc(componentId);
    }

    @Override
    public void delete(ComponentEntry entry) {
        jpaRepository.delete(entry);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public List<ComponentEntry> findByComponentIdAndStatusOrderBySortOrder(Long componentId,
            com.backend.domain.enums.ComponentStatus status) {
        return jpaRepository.findByComponentIdAndStatusOrderBySortOrderAsc(componentId, status);
    }
}
