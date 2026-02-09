package com.backend.presentation.dto.response;

public record LoginResponse(
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
        Boolean requires2FA,
        String pendingToken) {

    public LoginResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long userId,
            String email,
            String role,
            String subdomain,
            Long tenantId) {
        this(accessToken, refreshToken, tokenType, expiresIn, userId, email, null, role, subdomain, tenantId, false, null);
    }

    public LoginResponse {
        if (requires2FA != null && requires2FA) {
            if (pendingToken == null || pendingToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Pending token required when 2FA is required");
            }
        } else {
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Refresh token cannot be null or empty");
            }
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    public static LoginResponse requiring2FA(String email, String pendingToken, String subdomain, Long tenantId) {
        return new LoginResponse(
                null, null, null, null, null,
                email, null, null, subdomain, tenantId,
                true, pendingToken
        );
    }
}
