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

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.id = :id")
        Optional<Component> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId ORDER BY c.sortOrder ASC, c.id ASC")
        List<Component> findAllByTenantId(@Param("tenantId") Long tenantId);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.type = :type ORDER BY c.sortOrder ASC, c.id ASC")
        List<Component> findAllByTenantIdAndType(@Param("tenantId") Long tenantId,
                        @Param("type") ComponentType type);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.type = :type AND c.status = :status ORDER BY c.sortOrder ASC, c.id ASC")
        List<Component> findAllByTenantIdAndTypeAndStatus(@Param("tenantId") Long tenantId,
                        @Param("type") ComponentType type,
                        @Param("status") ComponentStatus status);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.type = :type AND c.key = :key")
        Optional<Component> findByTenantAndTypeAndKey(@Param("tenantId") Long tenantId,
                        @Param("type") ComponentType type,
                        @Param("key") String key);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId AND c.status = :status")
        List<Component> findByTenantAndStatus(@Param("tenantId") Long tenantId,
                        @Param("status") ComponentStatus status);

        @EntityGraph(attributePaths = { "translations" })
        @Query("SELECT c FROM Component c WHERE c.tenantId = :tenantId " +
                        "AND c.type = :type " +
                        "AND c.status = 'ACTIVE' " +
                        "AND c.visible = true " +
                        "ORDER BY c.sortOrder ASC, c.id ASC")
        List<Component> findActiveVisibleByTenantIdAndType(@Param("tenantId") Long tenantId,
                        @Param("type") ComponentType type);

        // =======================================================================================
        // SECURITY: Tenant Validation Methods - Critical for preventing cross-tenant data access
        // =======================================================================================

        /**
         * Finds all components by IDs that belong to the specified tenant.
         * This is used for batch tenant validation to prevent cross-tenant access.
         *
         * @param componentIds List of component IDs to find
         * @param tenantId Expected tenant ID for all components
         * @return List of components that belong to the tenant
         */
        @Query("SELECT c FROM Component c WHERE c.id IN :componentIds AND c.tenantId = :tenantId")
        List<Component> findAllByIdInAndTenantId(@Param("componentIds") List<Long> componentIds,
                                               @Param("tenantId") Long tenantId);

        /**
         * Checks if a component exists and belongs to the specified tenant.
         * Used for single component tenant validation.
         *
         * @param componentId Component ID to check
         * @param tenantId Expected tenant ID
         * @return true if component exists and belongs to tenant, false otherwise
         */
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Component c " +
               "WHERE c.id = :componentId AND c.tenantId = :tenantId")
        boolean existsByIdAndTenantId(@Param("componentId") Long componentId, @Param("tenantId") Long tenantId);

        /**
         * Counts components by IDs that belong to the specified tenant.
         * Used for optimized batch validation.
         *
         * @param componentIds List of component IDs to count
         * @param tenantId Expected tenant ID
         * @return Count of components that belong to the tenant
         */
        @Query("SELECT COUNT(c) FROM Component c WHERE c.id IN :componentIds AND c.tenantId = :tenantId")
        Long countByIdInAndTenantId(@Param("componentIds") List<Long> componentIds, @Param("tenantId") Long tenantId);
}