package com.backend.domain.port;

public interface JwtProviderPort {

    String createAccessToken(String email, String role, Long userId, Long tenantId);

    String createRefreshToken(String email, String role, Long userId, Long tenantId);

    String createRefreshToken(String email, String role, Long userId, Long tenantId, boolean rememberMe);

    boolean validateToken(String token);

    boolean isRefreshToken(String token);

    String getEmailFromToken(String token);

    String getRoleFromToken(String token);

    Long getTenantIdFromToken(String token);

    Long getUserIdFromToken(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();

    long getRefreshTokenExpiration(boolean rememberMe);

    boolean isRememberMeToken(String token);
}
