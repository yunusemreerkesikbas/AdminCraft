package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<Object[]> findAllPagedWithTypeNamesAndEntryCount(Pageable pageable);

    Page<Object[]> searchByQueryWithTypeNamesAndEntryCount(String query, Pageable pageable);

    List<Component> findByUidInAndStatus(List<String> uids, ComponentStatus status);

    List<Component> findByIdIn(List<Long> ids);

    List<Component> findByResponsiveMediaId(Long responsiveMediaId);

    boolean existsByNavigationNodeIdIn(List<Long> navigationNodeIds);

    long count();

    long countByStatus(ComponentStatus status);

    long countByCreatedAtAfter(LocalDateTime date);

    List<Component> findPublishedShellComponents();
}
