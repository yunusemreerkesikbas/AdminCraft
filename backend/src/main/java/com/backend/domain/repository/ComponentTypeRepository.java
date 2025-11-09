package com.backend.domain.repository;

import com.backend.domain.entity.ComponentType;
import java.util.List;
import java.util.Optional;

public interface ComponentTypeRepository {
    Optional<ComponentType> findById(Long id);

    Optional<ComponentType> findByUuid(String uuid);

    Optional<ComponentType> findByUid(String uid);

    Optional<ComponentType> findByCode(String code);

    List<ComponentType> findAll();

    List<ComponentType> findByCategory(String category);

    List<ComponentType> findByIsSystem(Boolean isSystem);

    ComponentType save(ComponentType entity);

    void delete(ComponentType entity);

    boolean existsByCode(String code);

    boolean existsByUid(String uid);
}
