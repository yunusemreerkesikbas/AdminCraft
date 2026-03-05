package com.backend.presentation.dto.response.config;

import com.backend.application.dto.config.ConfigAuthResult;

public record ConfigAuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        String fullName,
        String role,
        Long tenantId,
        String subdomain
) {
    public static ConfigAuthResponse from(ConfigAuthResult result) {
        return new ConfigAuthResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                result.userId(),
                result.email(),
                result.fullName(),
                result.role(),
                result.tenantId(),
                result.subdomain());
    }
}
