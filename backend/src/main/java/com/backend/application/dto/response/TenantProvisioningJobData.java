package com.backend.application.dto.response;

import java.time.LocalDateTime;

public record TenantProvisioningJobData(
        Long id,
        Long tenantId,
        String type,
        String status,
        Integer progress,
        String error,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt) {
}
