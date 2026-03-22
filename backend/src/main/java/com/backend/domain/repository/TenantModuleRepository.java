package com.backend.domain.repository;

import com.backend.domain.entity.TenantModule;

import java.util.List;

public interface TenantModuleRepository {

    List<TenantModule> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndModuleCodeAndStatus(Long tenantId, String moduleCode, String status);

    List<TenantModule> saveAll(Iterable<TenantModule> modules);
}
