package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ComponentType;

public interface ComponentTypeRepository {
    Optional<ComponentType> findById(Long id);

    Optional<ComponentType> findByUuid(String uuid);

    Optional<ComponentType> findByUid(String uid);

    List<ComponentType> findAll();

    List<ComponentType> findByCategory(String category);

    ComponentType save(ComponentType entity);

    void delete(ComponentType entity);

    boolean existsByUid(String uid);
}
