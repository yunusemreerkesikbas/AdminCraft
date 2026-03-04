package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.infrastructure.persistence.platform.entity.ConfigChangeAudit;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConfigChangeAuditRepositoryImpl implements ConfigChangeAuditRepository {

    private final JpaConfigChangeAuditRepository jpaRepository;

    @Override
    public ConfigChangeAudit save(ConfigChangeAudit entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public List<ConfigChangeAudit> findByTargetTenantIdOrderByCreatedAtDesc(Long tenantId, int limit) {
        int safeLimit = Math.max(1, Math.min(100, limit));
        return jpaRepository.findByTargetTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, safeLimit));
    }
}
