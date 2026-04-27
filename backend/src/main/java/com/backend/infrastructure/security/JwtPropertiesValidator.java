package com.backend.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SEC-004: Validates JWT secret strength and profile-aware fail-fast for placeholder
 * usage outside dev. HS512 requires a 512-bit (64-byte) key per RFC 7518.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtPropertiesValidator {

    private static final String DEV_PLACEHOLDER_PREFIX = "DEV_ONLY_PLACEHOLDER";
    private static final int MIN_SECRET_BYTES = 64;
    private static final long MIN_ACCESS_EXPIRATION_MS = 300_000L;
    private static final Set<String> DEV_PROFILES = Set.of("dev", "test");

    private final JwtProperties jwtProperties;
    private final Environment environment;

    @PostConstruct
    public void validate() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT secret must be configured. Set JWT_SECRET environment variable or app.jwt.secret.");
        }

        boolean isDevProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(DEV_PROFILES::contains);
        boolean isDevPlaceholder = secret.startsWith(DEV_PLACEHOLDER_PREFIX);

        if (!isDevProfile && isDevPlaceholder) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is required outside dev profile; "
                            + "the dev placeholder must never be used in stage/prod.");
        }

        int byteLength = secret.getBytes(StandardCharsets.UTF_8).length;
        if (!(isDevProfile && isDevPlaceholder) && byteLength < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_SECRET_BYTES
                            + " bytes (512 bits) for HS512; provided " + byteLength + " bytes.");
        }

        if (jwtProperties.getExpiration() < MIN_ACCESS_EXPIRATION_MS) {
            throw new IllegalStateException("Access token expiration too short (minimum 5 minutes)");
        }
        if (jwtProperties.getRefreshExpiration() < jwtProperties.getExpiration()) {
            throw new IllegalStateException("Refresh token expiration must be longer than access token");
        }

        if (isDevProfile && secret.startsWith(DEV_PLACEHOLDER_PREFIX)) {
            log.warn("SECURITY WARNING: JWT secret is the dev placeholder. Allowed only in dev profile.");
        } else {
            log.info("JWT secret validated ({} bytes).", byteLength);
        }
    }
}
