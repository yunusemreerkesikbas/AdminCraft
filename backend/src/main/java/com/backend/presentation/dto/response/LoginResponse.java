package com.backend.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        String tokenType,
        Long expiresIn,
        Long userId,
        String email,
        String fullName,
        String role,
        String subdomain,
        Long tenantId,
        Boolean requires2FA,
        String pendingToken,
        Integer resendCooldownSeconds) {

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
        this(accessToken, refreshToken, tokenType, expiresIn, userId, email, null, role, subdomain, tenantId, false, null, null);
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
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    public static LoginResponse requiring2FA(String email, String pendingToken, String subdomain, Long tenantId) {
        return requiring2FA(email, pendingToken, subdomain, tenantId, null);
    }

    public static LoginResponse requiring2FA(
            String email,
            String pendingToken,
            String subdomain,
            Long tenantId,
            Integer resendCooldownSeconds) {
        return new LoginResponse(
                null, null, null, null, null,
                email, null, null, subdomain, tenantId,
                true, pendingToken, resendCooldownSeconds
        );
    }
}
