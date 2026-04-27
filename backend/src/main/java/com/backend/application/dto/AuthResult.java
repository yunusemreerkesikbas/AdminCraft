package com.backend.application.dto;

public record AuthResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long userId,
        String email,
        String fullName,
        String role,
        String subdomain,
        Long tenantId,
        boolean requires2FA,
        String pendingToken,
        boolean rememberMe) {

    public static AuthResult requiring2FA(String email, String pendingToken, String subdomain, Long tenantId) {
        return new AuthResult(
                null, null, null, null, null,
                email, null, null, subdomain, tenantId,
                true, pendingToken, false);
    }

    public static AuthResult success(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long userId,
            String email,
            String fullName,
            String role,
            String subdomain,
            Long tenantId) {
        return new AuthResult(
                accessToken, refreshToken, tokenType, expiresIn,
                userId, email, fullName, role, subdomain, tenantId, false, null, false);
    }

    public static AuthResult success(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long userId,
            String email,
            String fullName,
            String role,
            String subdomain,
            Long tenantId,
            boolean rememberMe) {
        return new AuthResult(
                accessToken, refreshToken, tokenType, expiresIn,
                userId, email, fullName, role, subdomain, tenantId, false, null, rememberMe);
    }
}
