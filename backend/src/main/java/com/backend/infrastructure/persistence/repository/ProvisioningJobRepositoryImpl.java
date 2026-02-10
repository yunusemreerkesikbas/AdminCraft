package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.repository.ProvisioningJobRepository;

import lombok.RequiredArgsConstructor;

@Component("provisioningJobDomainRepository")
@RequiredArgsConstructor
public class ProvisioningJobRepositoryImpl implements ProvisioningJobRepository {

    private final com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository jpaRepository;

    @Override
    public ProvisioningJob save(ProvisioningJob job) {
        com.backend.infrastructure.persistence.platform.entity.ProvisioningJob platformJob = toPlatform(job);
        com.backend.infrastructure.persistence.platform.entity.ProvisioningJob saved = jpaRepository.save(platformJob);
        return toDomain(saved);
    }

    @Override
    public Optional<ProvisioningJob> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    private ProvisioningJob toDomain(com.backend.infrastructure.persistence.platform.entity.ProvisioningJob source) {
        if (source == null) return null;
        return ProvisioningJob.builder()
                .id(source.getId())
                .tenantId(source.getTenantId())
                .type(source.getType())
                .payload(source.getPayload())
                .status(source.getStatus())
                .progress(source.getProgress())
                .error(source.getError())
                .correlationId(source.getCorrelationId())
                .createdAt(source.getCreatedAt())
                .startedAt(source.getStartedAt())
                .completedAt(source.getCompletedAt())
                .build();
    }

    private com.backend.infrastructure.persistence.platform.entity.ProvisioningJob toPlatform(ProvisioningJob source) {
        if (source == null) return null;
        return com.backend.infrastructure.persistence.platform.entity.ProvisioningJob.builder()
                .id(source.getId())
                .tenantId(source.getTenantId())
                .type(source.getType())
                .payload(source.getPayload())
                .status(source.getStatus())
                .progress(source.getProgress())
                .error(source.getError())
                .correlationId(source.getCorrelationId())
                .createdAt(source.getCreatedAt())
                .startedAt(source.getStartedAt())
                .completedAt(source.getCompletedAt())
                .build();
    }
}
