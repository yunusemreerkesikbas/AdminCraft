package com.backend.application.dto.config;

import java.time.LocalDateTime;

public record ConfigAuditItemResult(
        Long id,
        Long actorUserId,
        String actorEmail,
        String actorRole,
        Long targetTenantId,
        String action,
        String reason,
        String beforeJson,
        String afterJson,
        String correlationId,
        LocalDateTime createdAt
) {
}
