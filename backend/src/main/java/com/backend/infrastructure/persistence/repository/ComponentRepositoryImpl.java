package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.repository.ComponentRepository;

import lombok.RequiredArgsConstructor;

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

    @Override
    public Page<Object[]> findAllPagedWithTypeNamesAndEntryCount(Pageable pageable) {
        return jpaRepository.findAllPagedWithTypeNamesAndEntryCount(pageable);
    }

    @Override
    public Page<Object[]> searchByQueryWithTypeNamesAndEntryCount(String query, Pageable pageable) {
        return jpaRepository.searchByQueryWithTypeNamesAndEntryCount(query, pageable);
    }

    @Override
    public List<Component> findByUidInAndStatus(List<String> uids, ComponentStatus status) {
        return jpaRepository.findByUidInAndStatus(uids, status);
    }

    @Override
    public List<Component> findByIdIn(List<Long> ids) {
        return jpaRepository.findByIdIn(ids);
    }

    @Override
    public List<Component> findByResponsiveMediaId(Long responsiveMediaId) {
        return jpaRepository.findByResponsiveMediaId(responsiveMediaId);
    }

    @Override
    public boolean existsByNavigationNodeIdIn(List<Long> navigationNodeIds) {
        return jpaRepository.existsByNavigationNodeIdIn(navigationNodeIds);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(ComponentStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long countByCreatedAtAfter(LocalDateTime date) {
        return jpaRepository.countByCreatedAtAfter(date);
    }

    @Override
    public List<Component> findPublishedShellComponents() {
        return jpaRepository.findPublishedShellComponents();
    }
}
