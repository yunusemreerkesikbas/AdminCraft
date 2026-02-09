package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;

public record ProvisioningJobResponse(
    Long id,
    Long tenantId,
    String type,
    String status,
    Integer progress,
    String error,
    LocalDateTime createdAt,
    LocalDateTime startedAt,
    LocalDateTime completedAt
) {
    public static ProvisioningJobResponse from(ProvisioningJob job) {
        return new ProvisioningJobResponse(
            job.getId(),
            job.getTenantId(),
            job.getType(),
            job.getStatus(),
            job.getProgress(),
            job.getError(),
            job.getCreatedAt(),
            job.getStartedAt(),
            job.getCompletedAt()
        );
    }
}
