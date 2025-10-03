package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.enums.JobStatus;
import com.backend.domain.enums.JobType;

import java.time.LocalDateTime;

public record ProvisioningJobResponse(
        String uuid,
        Long tenantId,
        JobType jobType,
        JobStatus status,
        String languages,
        Integer totalItems,
        Integer processedItems,
        Integer failedItems,
        Integer progressPercentage,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt) {

    public static ProvisioningJobResponse fromEntity(ProvisioningJob job) {
        return new ProvisioningJobResponse(
                job.getUuid(),
                job.getTenantId(),
                job.getJobType(),
                job.getStatus(),
                job.getLanguages(),
                job.getTotalItems(),
                job.getProcessedItems(),
                job.getFailedItems(),
                (int) Math.round(job.getProgressPercentage()),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt());
    }
}
