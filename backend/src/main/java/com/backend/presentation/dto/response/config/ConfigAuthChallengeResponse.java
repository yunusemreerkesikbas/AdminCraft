package com.backend.presentation.dto.response.config;

import com.backend.application.dto.config.ConfigAuthChallengeResult;

public record ConfigAuthChallengeResponse(
        String pendingToken,
        String email,
        Long tenantId,
        String subdomain,
        String role
) {
    public static ConfigAuthChallengeResponse from(ConfigAuthChallengeResult result) {
        return new ConfigAuthChallengeResponse(
                result.pendingToken(),
                result.email(),
                result.tenantId(),
                result.subdomain(),
                result.role());
    }
}
