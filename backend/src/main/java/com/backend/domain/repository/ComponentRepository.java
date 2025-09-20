package com.backend.domain.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository {
  Optional<Component> findByIdAndTenantId(Long id, Long tenantId);

  List<Component> findAllByTenantId(Long tenantId);

  List<Component> findAllByTenantIdAndType(Long tenantId, ComponentType type);

  List<Component> findAllByTenantIdAndTypeAndStatus(Long tenantId, ComponentType type, ComponentStatus status);

  Optional<Component> findByTenantAndTypeAndKey(Long tenantId, ComponentType type, String key);

  List<Component> findByTenantAndStatus(Long tenantId, ComponentStatus status);

  List<Component> findActiveVisibleByTenantIdAndType(Long tenantId, ComponentType type);

  Component save(Component component);

  void delete(Component component);

  // =======================================================================================
  // SECURITY: Tenant Validation Methods - Critical for preventing cross-tenant data access
  // =======================================================================================

  /**
   * Validates that all component IDs belong to the specified tenant.
   * This is critical for preventing cross-tenant data access vulnerabilities.
   *
   * @param componentIds List of component IDs to validate
   * @param tenantId Expected tenant ID
   * @return List of components that belong to the tenant
   * @throws com.backend.domain.exception.TenantSecurityException if any component doesn't belong to tenant
   */
  List<Component> findAllByIdInAndTenantId(List<Long> componentIds, Long tenantId);

  /**
   * Checks if a component exists and belongs to the specified tenant.
   * Used for single component validation.
   *
   * @param componentId Component ID to check
   * @param tenantId Expected tenant ID
   * @return true if component exists and belongs to tenant
   */
  boolean existsByIdAndTenantId(Long componentId, Long tenantId);

  /**
   * Validates that a single component belongs to the specified tenant.
   * Throws exception if component doesn't exist or belongs to different tenant.
   *
   * @param componentId Component ID to validate
   * @param tenantId Expected tenant ID
   * @throws com.backend.domain.exception.TenantSecurityException if validation fails
   */
  void validateComponentBelongsToTenant(Long componentId, Long tenantId);

  /**
   * Validates that all component IDs in the list belong to the specified tenant.
   * Throws exception if any component doesn't exist or belongs to different tenant.
   *
   * @param componentIds List of component IDs to validate
   * @param tenantId Expected tenant ID
   * @throws com.backend.domain.exception.TenantSecurityException if validation fails
   */
  void validateComponentsBelongToTenant(List<Long> componentIds, Long tenantId);
}