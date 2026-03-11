package com.backend.infrastructure.security;

import org.springframework.stereotype.Component;

import com.backend.domain.port.JwtProviderPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProviderPortImpl implements JwtProviderPort {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String createAccessToken(String email, String role, Long userId, Long tenantId) {
        return jwtTokenProvider.createAccessToken(email, role, userId, tenantId);
    }

    @Override
    public String createRefreshToken(String email) {
        return jwtTokenProvider.createRefreshToken(email);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public boolean isRefreshToken(String token) {
        return jwtTokenProvider.isRefreshToken(token);
    }

    @Override
    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    @Override
    public String getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }

    @Override
    public Long getTenantIdFromToken(String token) {
        return jwtTokenProvider.getTenantIdFromToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @Override
    public long getAccessTokenExpiration() {
        return jwtTokenProvider.getAccessTokenExpiration();
    }

    @Override
    public long getRefreshTokenExpiration() {
        return jwtTokenProvider.getRefreshTokenExpiration();
    }
}
