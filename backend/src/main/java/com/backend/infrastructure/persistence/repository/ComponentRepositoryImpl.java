package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentItem;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.exception.TenantSecurityException;
import com.backend.domain.repository.ComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ComponentRepositoryImpl implements ComponentRepository {

  private static final Logger logger = LoggerFactory.getLogger(ComponentRepositoryImpl.class);

  private final ComponentJpaRepository jpaRepository;
  private final ComponentItemJpaRepository itemJpaRepository;

  public ComponentRepositoryImpl(ComponentJpaRepository jpaRepository,
      ComponentItemJpaRepository itemJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.itemJpaRepository = itemJpaRepository;
  }

  @Override
  public Optional<Component> findByIdAndTenantId(Long id, Long tenantId) {
    return jpaRepository.findByIdAndTenantId(id, tenantId);
  }

  @Override
  public List<Component> findAllByTenantId(Long tenantId) {
    return jpaRepository.findAllByTenantId(tenantId);
  }

  @Override
  public List<Component> findAllByTenantIdAndType(Long tenantId, ComponentType type) {
    return jpaRepository.findAllByTenantIdAndType(tenantId, type);
  }

  @Override
  public List<Component> findAllByTenantIdAndTypeAndStatus(Long tenantId, ComponentType type, ComponentStatus status) {
    return jpaRepository.findAllByTenantIdAndTypeAndStatus(tenantId, type, status);
  }

  @Override
  public Optional<Component> findByTenantAndTypeAndKey(Long tenantId, ComponentType type, String key) {
    return jpaRepository.findByTenantAndTypeAndKey(tenantId, type, key);
  }

  @Override
  public List<Component> findByTenantAndStatus(Long tenantId, ComponentStatus status) {
    return jpaRepository.findByTenantAndStatus(tenantId, status);
  }

  @Override
  public List<Component> findActiveVisibleByTenantIdAndType(Long tenantId, ComponentType type) {
    return jpaRepository.findActiveVisibleByTenantIdAndType(tenantId, type);
  }

  @Override
  public Component save(Component component) {
    return jpaRepository.save(component);
  }

  @Override
  public void delete(Component component) {
    jpaRepository.delete(component);
  }

  @Override
  public List<Component> findAllByIdInAndTenantId(List<Long> componentIds, Long tenantId) {
    if (componentIds == null || componentIds.isEmpty()) {
      return List.of();
    }

    List<Component> components = jpaRepository.findAllByIdInAndTenantId(componentIds, tenantId);

    if (components.size() != componentIds.size()) {
      Set<Long> foundIds = components.stream().map(Component::getId).collect(Collectors.toSet());
      List<Long> missingIds = componentIds.stream()
          .filter(id -> !foundIds.contains(id))
          .collect(Collectors.toList());

      logger.error("SECURITY VIOLATION: Some components not found or don't belong to tenant {}. " +
          "Requested: {}, Found: {}, Missing: {}",
          tenantId, componentIds, foundIds, missingIds);

      throw TenantSecurityException.invalidBatchComponentAccess(tenantId);
    }

    logger.debug("Validated {} components belong to tenant {}", components.size(), tenantId);
    return components;
  }

  @Override
  public boolean existsByIdAndTenantId(Long componentId, Long tenantId) {
    if (componentId == null || tenantId == null) {
      return false;
    }

    boolean exists = jpaRepository.existsByIdAndTenantId(componentId, tenantId);
    logger.debug("Component {} exists for tenant {}: {}", componentId, tenantId, exists);
    return exists;
  }

  @Override
  public void validateComponentBelongsToTenant(Long componentId, Long tenantId) {
    if (componentId == null || tenantId == null) {
      logger.error("SECURITY VIOLATION: Null component ID {} or tenant ID {} in validation",
          componentId, tenantId);
      throw TenantSecurityException.invalidComponentAccess(componentId, tenantId);
    }

    if (!existsByIdAndTenantId(componentId, tenantId)) {
      logger.error("SECURITY VIOLATION: Component {} does not belong to tenant {} or doesn't exist",
          componentId, tenantId);
      throw TenantSecurityException.invalidComponentAccess(componentId, tenantId);
    }

    logger.debug("Component {} validated for tenant {}", componentId, tenantId);
  }

  @Override
  public void validateComponentsBelongToTenant(List<Long> componentIds, Long tenantId) {
    if (componentIds == null || componentIds.isEmpty()) {
      return; // Empty list is valid
    }

    if (tenantId == null) {
      logger.error("SECURITY VIOLATION: Null tenant ID in batch validation for components: {}",
          componentIds);
      throw TenantSecurityException.invalidBatchComponentAccess(tenantId);
    }

    try {
      findAllByIdInAndTenantId(componentIds, tenantId);
      logger.debug("Batch validation successful for {} components in tenant {}",
          componentIds.size(), tenantId);
    } catch (TenantSecurityException e) {
      throw e;
    } catch (Exception e) {
      logger.error("SECURITY VIOLATION: Unexpected error during batch validation for tenant {}. " +
          "Component IDs: {}, Error: {}", tenantId, componentIds, e.getMessage());
      throw TenantSecurityException.invalidBatchComponentAccess(tenantId);
    }
  }

  // NAVBAR detail: flat item entries
  public List<ComponentItem> findItemsByComponentId(Long componentId) {
    return itemJpaRepository.findAllByComponentId(componentId);
  }
}