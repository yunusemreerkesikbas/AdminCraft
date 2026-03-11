package com.backend.application.dto.config;

public record ConfigAuthChallengeResult(
        String pendingToken,
        String email,
        Long tenantId,
        String subdomain,
        String role
) {
}
