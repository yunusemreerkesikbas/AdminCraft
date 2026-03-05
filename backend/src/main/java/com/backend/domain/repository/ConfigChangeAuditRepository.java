package com.backend.domain.repository;

import java.util.List;

import com.backend.domain.entity.ConfigChangeAudit;

public interface ConfigChangeAuditRepository {

    ConfigChangeAudit save(ConfigChangeAudit entity);

    List<ConfigChangeAudit> findByTargetTenantIdOrderByCreatedAtDesc(Long tenantId, int limit);
}
