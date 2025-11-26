package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentType;
import com.backend.domain.repository.ComponentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ComponentTypeRepositoryImpl implements ComponentTypeRepository {

    private final ComponentTypeJpaRepository jpaRepository;

    @Override
    public Optional<ComponentType> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ComponentType> findByUuid(String uuid) {
        return jpaRepository.findByUuid(uuid);
    }

    @Override
    public Optional<ComponentType> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<ComponentType> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<ComponentType> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<ComponentType> findByCategory(String category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public List<ComponentType> findByIsSystem(Boolean isSystem) {
        return jpaRepository.findByIsSystem(isSystem);
    }

    @Override
    public ComponentType save(ComponentType entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(ComponentType entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }
}

