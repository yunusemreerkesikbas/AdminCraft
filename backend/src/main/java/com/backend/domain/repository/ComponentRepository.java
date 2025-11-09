package com.backend.domain.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import java.util.List;
import java.util.Optional;

public interface ComponentRepository {
    Optional<Component> findById(Long id);
    Optional<Component> findByUuid(String uuid);
    Optional<Component> findByUid(String uid);
    Optional<Component> findByCode(String code);
    List<Component> findAll();
    List<Component> findByComponentTypeId(Long componentTypeId);
    List<Component> findByStatus(ComponentStatus status);
    Component save(Component entity);
    void delete(Component entity);
    boolean existsByCode(String code);
    boolean existsByUid(String uid);
}
