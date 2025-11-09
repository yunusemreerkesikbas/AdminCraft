package com.backend.domain.repository;

import com.backend.domain.entity.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Long> {
    Optional<ComponentType> findByUuid(String uuid);
    Optional<ComponentType> findByUid(String uid);
    Optional<ComponentType> findByCode(String code);
    List<ComponentType> findByCategory(String category);
    List<ComponentType> findByIsSystem(Boolean isSystem);
    boolean existsByCode(String code);
    boolean existsByUid(String uid);
}
