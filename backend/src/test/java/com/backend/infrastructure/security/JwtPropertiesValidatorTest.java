package com.backend.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtPropertiesValidatorTest {

    private static final String DEV_PLACEHOLDER =
            "DEV_ONLY_PLACEHOLDER_DO_NOT_USE_IN_PROD_MIN_64_BYTES_AAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String VALID_64_BYTE_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @Test
    @DisplayName("SEC-004: dev profile accepts dev placeholder")
    void validate_ShouldAcceptPlaceholder_InDev() {
        JwtPropertiesValidator validator = newValidator(DEV_PLACEHOLDER, "dev");

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SEC-004: prod profile rejects dev placeholder")
    void validate_ShouldRejectPlaceholder_InProd() {
        JwtPropertiesValidator validator = newValidator(DEV_PLACEHOLDER, "prod");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dev placeholder");
    }

    @Test
    @DisplayName("SEC-004: stage profile rejects dev placeholder")
    void validate_ShouldRejectPlaceholder_InStage() {
        JwtPropertiesValidator validator = newValidator(DEV_PLACEHOLDER, "stage");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dev placeholder");
    }

    @Test
    @DisplayName("SEC-004: secret shorter than 64 bytes is rejected")
    void validate_ShouldReject_WhenSecretShorterThan64Bytes() {
        String tooShort = "0123456789012345678901234567890123456789"; // 40 bytes
        JwtPropertiesValidator validator = newValidator(tooShort, "prod");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("64 bytes");
    }

    @Test
    @DisplayName("SEC-004: prod profile accepts a real 64-byte secret")
    void validate_ShouldAccept_ValidSecret_InProd() {
        JwtPropertiesValidator validator = newValidator(VALID_64_BYTE_SECRET, "prod");

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SEC-004: null secret fails fast with clear message")
    void validate_ShouldReject_NullSecret() {
        JwtPropertiesValidator validator = newValidator(null, "dev");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret");
    }

    @Test
    @DisplayName("SEC-004: empty secret fails fast")
    void validate_ShouldReject_EmptySecret() {
        JwtPropertiesValidator validator = newValidator("   ", "dev");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret");
    }

    @Test
    @DisplayName("Access token expiration must be at least 5 minutes")
    void validate_ShouldReject_WhenAccessExpirationTooShort() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_64_BYTE_SECRET);
        properties.setExpiration(60_000L); // 1 minute
        properties.setRefreshExpiration(86_400_000L);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtPropertiesValidator validator = new JwtPropertiesValidator(properties, env);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Access token expiration");
    }

    @Test
    @DisplayName("Refresh token expiration must exceed access token")
    void validate_ShouldReject_WhenRefreshShorterThanAccess() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_64_BYTE_SECRET);
        properties.setExpiration(86_400_000L);
        properties.setRefreshExpiration(3_600_000L);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtPropertiesValidator validator = new JwtPropertiesValidator(properties, env);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refresh token expiration");
    }

    private JwtPropertiesValidator newValidator(String secret, String activeProfile) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(activeProfile);
        return new JwtPropertiesValidator(properties, env);
    }
}
