package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.repository.ComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComponentRepositoryImpl implements ComponentRepository {

  private final ComponentJpaRepository jpaRepository;

  public ComponentRepositoryImpl(ComponentJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
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
}