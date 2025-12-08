package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

public interface ComponentRepository {
    Optional<Component> findById(Long id);

    Optional<Component> findByUuid(String uuid);

    Optional<Component> findByUid(String uid);

    List<Component> findAll();

    List<Component> findByComponentTypeId(Long componentTypeId);

    List<Component> findByStatus(ComponentStatus status);

    Component save(Component entity);

    void delete(Component entity);

    boolean existsByUid(String uid);

    List<Object[]> findAllWithTypeNames();

    List<Object[]> findAllWithTypeNamesAndEntryCount();

    List<Component> findByUidInAndStatus(List<String> uids, ComponentStatus status);

    List<Component> findByIdIn(List<Long> ids);
}
