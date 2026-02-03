package com.backend.application.dto;

public record AuthResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long userId,
        String email,
        String role,
        String subdomain,
        Long tenantId,
        boolean requires2FA,
        String pendingToken) {

    public AuthResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long userId,
            String email,
            String role,
            String subdomain,
            Long tenantId) {
        this(accessToken, refreshToken, tokenType, expiresIn, userId, email, role, subdomain, tenantId, false, null);
    }

    public static AuthResult requiring2FA(String email, String pendingToken, String subdomain, Long tenantId) {
        return new AuthResult(
                null, null, null, null, null,
                email, null, subdomain, tenantId,
                true, pendingToken);
    }

    public static AuthResult success(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long userId,
            String email,
            String role,
            String subdomain,
            Long tenantId) {
        return new AuthResult(
                accessToken, refreshToken, tokenType, expiresIn,
                userId, email, role, subdomain, tenantId, false, null);
    }
}
