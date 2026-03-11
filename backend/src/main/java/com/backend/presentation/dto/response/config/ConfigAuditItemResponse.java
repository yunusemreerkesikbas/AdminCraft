package com.backend.presentation.dto.response.config;

import java.time.LocalDateTime;

import com.backend.application.dto.config.ConfigAuditItemResult;

public record ConfigAuditItemResponse(
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
    public static ConfigAuditItemResponse from(ConfigAuditItemResult result) {
        return new ConfigAuditItemResponse(
                result.id(),
                result.actorUserId(),
                result.actorEmail(),
                result.actorRole(),
                result.targetTenantId(),
                result.action(),
                result.reason(),
                result.beforeJson(),
                result.afterJson(),
                result.correlationId(),
                result.createdAt());
    }
}
