package com.backend.application.dto.config;

public record ConfigAuthResult(
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
}
