package com.backend.domain.repository;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository {
  Optional<Component> findByIdAndTenantId(Long id, Long tenantId);

  List<Component> findAllByTenantId(Long tenantId);

  Optional<Component> findByTenantAndTypeAndKey(Long tenantId, ComponentType type, String key);

  List<Component> findByTenantAndStatus(Long tenantId, ComponentStatus status);

  Component save(Component component);

  void delete(Component component);
}
