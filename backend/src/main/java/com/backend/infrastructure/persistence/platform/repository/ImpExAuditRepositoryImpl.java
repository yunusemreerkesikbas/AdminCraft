package com.backend.infrastructure.persistence.platform.repository;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ImpExAudit;
import com.backend.domain.repository.ImpExAuditRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ImpExAuditRepositoryImpl implements ImpExAuditRepository {

    private final JpaImpExAuditRepository jpaRepository;

    @Override
    public ImpExAudit save(ImpExAudit entity) {
        return toDomain(jpaRepository.save(toEntity(entity)));
    }

    private ImpExAudit toDomain(com.backend.infrastructure.persistence.platform.entity.ImpExAudit source) {
        if (source == null) {
            return null;
        }
        return ImpExAudit.builder()
                .id(source.getId())
                .executedAt(source.getExecutedAt())
                .actorUserId(source.getActorUserId())
                .actorEmail(source.getActorEmail())
                .actorRole(source.getActorRole())
                .tenantId(source.getTenantId())
                .tenantDb(source.getTenantDb())
                .fullSql(source.getFullSql())
                .statementCount(source.getStatementCount())
                .successCount(source.getSuccessCount())
                .failedCount(source.getFailedCount())
                .status(source.getStatus())
                .correlationId(source.getCorrelationId())
                .clientIp(source.getClientIp())
                .durationMs(source.getDurationMs())
                .build();
    }

    private com.backend.infrastructure.persistence.platform.entity.ImpExAudit toEntity(ImpExAudit source) {
        if (source == null) {
            return null;
        }
        return com.backend.infrastructure.persistence.platform.entity.ImpExAudit.builder()
                .id(source.getId())
                .executedAt(source.getExecutedAt())
                .actorUserId(source.getActorUserId())
                .actorEmail(source.getActorEmail())
                .actorRole(source.getActorRole())
                .tenantId(source.getTenantId())
                .tenantDb(source.getTenantDb())
                .fullSql(source.getFullSql())
                .statementCount(source.getStatementCount())
                .successCount(source.getSuccessCount())
                .failedCount(source.getFailedCount())
                .status(source.getStatus())
                .correlationId(source.getCorrelationId())
                .clientIp(source.getClientIp())
                .durationMs(source.getDurationMs())
                .build();
    }
}
