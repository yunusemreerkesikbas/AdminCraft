package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentJpaRepository extends JpaRepository<Component, Long> {
    Optional<Component> findByUuid(String uuid);
    Optional<Component> findByUid(String uid);
    Optional<Component> findByCode(String code);
    List<Component> findByComponentTypeId(Long componentTypeId);
    List<Component> findByStatus(ComponentStatus status);
    boolean existsByCode(String code);
    boolean existsByUid(String uid);
}
