package com.backend.application.dto.config;

public record ConfigAuthResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long issuedAt,
        Long userId,
        String email,
        String fullName,
        String role,
        Long tenantId,
        String subdomain
) {
}
