package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ComponentRepositoryImpl implements ComponentRepository {

    private final ComponentJpaRepository jpaRepository;

    @Override
    public Optional<Component> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Component> findByUuid(String uuid) {
        return jpaRepository.findByUuid(uuid);
    }

    @Override
    public Optional<Component> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public List<Component> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Component> findByComponentTypeId(Long componentTypeId) {
        return jpaRepository.findByComponentTypeId(componentTypeId);
    }

    @Override
    public List<Component> findByStatus(ComponentStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public Component save(Component entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(Component entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public List<Object[]> findAllWithTypeNames() {
        return jpaRepository.findAllWithTypeNames();
    }

    @Override
    public List<Object[]> findAllWithTypeNamesAndEntryCount() {
        return jpaRepository.findAllWithTypeNamesAndEntryCount();
    }
}

