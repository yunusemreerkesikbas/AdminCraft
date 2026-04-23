package com.backend.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final long rememberMeExpiration;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        log.info("Initializing JwtTokenProvider with secret length: {}", jwtProperties.getSecret().length());
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpiration = jwtProperties.getExpiration();
        this.refreshTokenExpiration = jwtProperties.getRefreshExpiration();
        this.rememberMeExpiration = jwtProperties.getRememberMeExpiration();
    }

    public String createAccessToken(String email, String role, Long userId, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate);

        // Add userId claim (required for multi-tenant operations)
        if (userId != null) {
            builder.claim("userId", userId);
        }

        // Only add tenantId if it's not null (SUPER_ADMIN users might not have a tenantId)
        if (tenantId != null) {
            builder.claim("tenantId", tenantId);
        }

        return builder.signWith(secretKey).compact();
    }

    public String createRefreshToken(String email, String role, Long userId, Long tenantId) {
        return createRefreshToken(email, role, userId, tenantId, refreshTokenExpiration);
    }

    public String createRefreshToken(String email, String role, Long userId, Long tenantId, boolean rememberMe) {
        long ttl = rememberMe ? rememberMeExpiration : refreshTokenExpiration;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl);

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate);

        if (role != null) {
            builder.claim("role", role);
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        if (tenantId != null) {
            builder.claim("tenantId", tenantId);
        }

        if (rememberMe) {
            builder.claim("rememberMe", true);
        }

        return builder.signWith(secretKey).compact();
    }

    public String createRefreshToken(String email, String role, Long userId, Long tenantId, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate);

        if (role != null) {
            builder.claim("role", role);
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        if (tenantId != null) {
            builder.claim("tenantId", tenantId);
        }

        return builder.signWith(secretKey).compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Long getTenantIdFromToken(String token) {
        Object tenantIdObj = parseToken(token).get("tenantId");
        if (tenantIdObj == null) {
            return null;
        }

        if (tenantIdObj instanceof Number) {
            return ((Number) tenantIdObj).longValue();
        }

        try {
            return Long.parseLong(tenantIdObj.toString());
        } catch (NumberFormatException ex) {
            log.warn("Invalid tenantId format in token: {}", tenantIdObj);
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        Object userIdObj = parseToken(token).get("userId");
        if (userIdObj == null) {
            return null;
        }

        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }

        try {
            return Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException ex) {
            log.warn("Invalid userId format in token: {}", userIdObj);
            return null;
        }
    }

    public boolean isRememberMeToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Boolean.TRUE.equals(claims.get("rememberMe", Boolean.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public long getRefreshTokenExpiration(boolean rememberMe) {
        return rememberMe ? rememberMeExpiration : refreshTokenExpiration;
    }

    public long getRememberMeExpiration() {
        return rememberMeExpiration;
    }
    
    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }
    
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenType(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            log.debug("Failed to determine token type: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenType(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.debug("Failed to determine token type: {}", e.getMessage());
            return false;
        }
    }
}
