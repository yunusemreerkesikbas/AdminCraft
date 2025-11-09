package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

interface ComponentTypeJpaRepository extends JpaRepository<ComponentType, Long> {
    Optional<ComponentType> findByUuid(String uuid);
    Optional<ComponentType> findByUid(String uid);
    Optional<ComponentType> findByCode(String code);
    List<ComponentType> findByCategory(String category);
    List<ComponentType> findByIsSystem(Boolean isSystem);
    boolean existsByCode(String code);
    boolean existsByUid(String uid);
}
