package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

interface ComponentJpaRepository extends JpaRepository<Component, Long> {
    Optional<Component> findByUuid(String uuid);

    Optional<Component> findByUid(String uid);

    List<Component> findByComponentTypeId(Long componentTypeId);

    List<Component> findByStatus(ComponentStatus status);

    boolean existsByUid(String uid);

    @Query("""
                SELECT c, ct.name
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
                ORDER BY c.updatedAt DESC
            """)
    List<Object[]> findAllWithTypeNames();

    @Query("""
                SELECT c, ct.name,
                       (SELECT COUNT(e) FROM ComponentEntry e WHERE e.componentId = c.id)
                FROM Component c
                LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
                ORDER BY c.updatedAt DESC
            """)
    List<Object[]> findAllWithTypeNamesAndEntryCount();

    @Query("SELECT c FROM Component c WHERE c.id = :id")
    Optional<Component> findByIdOptimized(@Param("id") Long id);
}
