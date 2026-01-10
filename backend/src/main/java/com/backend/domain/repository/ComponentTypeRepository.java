package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.ComponentType;

public interface ComponentTypeRepository {
    Optional<ComponentType> findById(Long id);

    Optional<ComponentType> findByUuid(String uuid);

    Optional<ComponentType> findByUid(String uid);

    List<ComponentType> findAll();

    Page<ComponentType> findAllPaged(Pageable pageable);

    Page<ComponentType> searchPaged(String query, Pageable pageable);

    List<ComponentType> findByCategory(String category);

    List<ComponentType> findByIdIn(List<Long> ids);

    ComponentType save(ComponentType entity);

    void delete(ComponentType entity);

    boolean existsByUid(String uid);
}
