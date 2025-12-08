package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ComponentType;
import com.backend.domain.repository.ComponentTypeRepository;

import lombok.RequiredArgsConstructor;

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
    public List<ComponentType> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<ComponentType> findByCategory(String category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public List<ComponentType> findByIdIn(List<Long> ids) {
        return jpaRepository.findByIdIn(ids);
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
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

}
