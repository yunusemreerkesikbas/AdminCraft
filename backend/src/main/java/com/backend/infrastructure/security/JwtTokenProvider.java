package com.backend.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        log.info("Initializing JwtTokenProvider with secret length: {}", jwtProperties.getSecret().length());
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpiration = jwtProperties.getExpiration();
        this.refreshTokenExpiration = jwtProperties.getRefreshExpiration();
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

    // Backward compatibility - keep the old method for refresh tokens
    public String createAccessToken(String email, String role) {
        return createAccessToken(email, role, null, null);
    }

    // Backward compatibility - for migration
    public String createAccessToken(String email, String role, Long tenantId) {
        return createAccessToken(email, role, null, tenantId);
    }

    public String createRefreshToken(String email, String role, Long userId, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

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

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("role", String.class);
    }

    public Long getTenantIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object tenantIdObj = claims.get("tenantId");
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
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userIdObj = claims.get("userId");
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
    
    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("type", String.class);
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
