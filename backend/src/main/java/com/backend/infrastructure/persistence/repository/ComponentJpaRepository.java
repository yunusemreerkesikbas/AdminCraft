package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

interface ComponentJpaRepository extends JpaRepository<Component, Long> {
    Optional<Component> findByUuid(String uuid);
    Optional<Component> findByUid(String uid);
    Optional<Component> findByCode(String code);
    List<Component> findByComponentTypeId(Long componentTypeId);
    List<Component> findByStatus(ComponentStatus status);
    boolean existsByCode(String code);
    boolean existsByUid(String uid);

    @Query("""
        SELECT c, ct.name
        FROM Component c
        LEFT JOIN ComponentType ct ON c.componentTypeId = ct.id
        ORDER BY c.updatedAt DESC
    """)
    List<Object[]> findAllWithTypeNames();
}
