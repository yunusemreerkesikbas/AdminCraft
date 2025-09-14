package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentJpaRepository extends JpaRepository<Component, Long> {

    @EntityGraph(attributePaths = {"translations"})
    @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.id = :id")
    Optional<Component> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @EntityGraph(attributePaths = {"translations"})
    @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId ORDER BY c.sortOrder ASC, c.id ASC")
    List<Component> findAllByTenantId(@Param("tenantId") Long tenantId);

    @EntityGraph(attributePaths = {"translations"})
    @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.type = :type ORDER BY c.sortOrder ASC, c.id ASC")
    List<Component> findAllByTenantIdAndType(@Param("tenantId") Long tenantId,
            @Param("type") ComponentType type);

    @EntityGraph(attributePaths = {"translations"})
    @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.type = :type AND c.key = :key")
    Optional<Component> findByTenantAndTypeAndKey(@Param("tenantId") Long tenantId,
            @Param("type") ComponentType type,
            @Param("key") String key);

    @EntityGraph(attributePaths = {"translations"})
    @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.status = :status")
    List<Component> findByTenantAndStatus(@Param("tenantId") Long tenantId,
            @Param("status") ComponentStatus status);
}
