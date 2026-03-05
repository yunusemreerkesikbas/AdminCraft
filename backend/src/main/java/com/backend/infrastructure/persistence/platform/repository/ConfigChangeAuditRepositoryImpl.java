package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.entity.ConfigChangeAudit;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConfigChangeAuditRepositoryImpl implements ConfigChangeAuditRepository {

    private final JpaConfigChangeAuditRepository jpaRepository;

    @Override
    public ConfigChangeAudit save(ConfigChangeAudit entity) {
        return toDomain(jpaRepository.save(toEntity(entity)));
    }

    @Override
    public List<ConfigChangeAudit> findByTargetTenantIdOrderByCreatedAtDesc(Long tenantId, int limit) {
        int safeLimit = Math.max(1, Math.min(100, limit));
        return jpaRepository.findByTargetTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ConfigChangeAudit toDomain(com.backend.infrastructure.persistence.platform.entity.ConfigChangeAudit source) {
        if (source == null) {
            return null;
        }
        return ConfigChangeAudit.builder()
                .id(source.getId())
                .actorUserId(source.getActorUserId())
                .actorEmail(source.getActorEmail())
                .actorRole(source.getActorRole())
                .targetTenantId(source.getTargetTenantId())
                .scope(source.getScope())
                .action(source.getAction())
                .beforeJson(source.getBeforeJson())
                .afterJson(source.getAfterJson())
                .reason(source.getReason())
                .correlationId(source.getCorrelationId())
                .createdAt(source.getCreatedAt())
                .build();
    }

    private com.backend.infrastructure.persistence.platform.entity.ConfigChangeAudit toEntity(ConfigChangeAudit source) {
        if (source == null) {
            return null;
        }
        return com.backend.infrastructure.persistence.platform.entity.ConfigChangeAudit.builder()
                .id(source.getId())
                .actorUserId(source.getActorUserId())
                .actorEmail(source.getActorEmail())
                .actorRole(source.getActorRole())
                .targetTenantId(source.getTargetTenantId())
                .scope(source.getScope())
                .action(source.getAction())
                .beforeJson(source.getBeforeJson())
                .afterJson(source.getAfterJson())
                .reason(source.getReason())
                .correlationId(source.getCorrelationId())
                .createdAt(source.getCreatedAt())
                .build();
    }
}
