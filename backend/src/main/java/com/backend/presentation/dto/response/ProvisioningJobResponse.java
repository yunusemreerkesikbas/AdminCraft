package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.application.dto.response.TenantProvisioningJobData;

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
    public static ProvisioningJobResponse from(TenantProvisioningJobData job) {
        return new ProvisioningJobResponse(
            job.id(),
            job.tenantId(),
            job.type(),
            job.status(),
            job.progress(),
            job.error(),
            job.createdAt(),
            job.startedAt(),
            job.completedAt()
        );
    }
}
