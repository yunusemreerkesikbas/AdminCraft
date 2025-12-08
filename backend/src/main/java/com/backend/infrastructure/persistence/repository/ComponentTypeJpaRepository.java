package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.ComponentType;

interface ComponentTypeJpaRepository extends JpaRepository<ComponentType, Long> {
    Optional<ComponentType> findByUuid(String uuid);

    Optional<ComponentType> findByUid(String uid);

    List<ComponentType> findByCategory(String category);

    List<ComponentType> findByIdIn(List<Long> ids);

    boolean existsByUid(String uid);
}
