package com.backend.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class OtpPropertiesTest {

    @Test
    @DisplayName("validateBypassCode should keep bypass code in dev profile")
    void validateBypassCode_ShouldKeepBypassCode_InDev() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        OtpProperties properties = new OtpProperties(environment);
        properties.setBypassCode("123456");

        properties.validateBypassCode();

        assertThat(properties.getBypassCode()).isEqualTo("123456");
    }

    @Test
    @DisplayName("validateBypassCode should keep bypass code in stage profile")
    void validateBypassCode_ShouldKeepBypassCode_InStage() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("stage");
        OtpProperties properties = new OtpProperties(environment);
        properties.setBypassCode("123456");

        properties.validateBypassCode();

        assertThat(properties.getBypassCode()).isEqualTo("123456");
    }

    @Test
    @DisplayName("validateBypassCode should clear bypass code outside dev and stage")
    void validateBypassCode_ShouldClearBypassCode_OutsideDevAndStage() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        OtpProperties properties = new OtpProperties(environment);
        properties.setBypassCode("123456");

        properties.validateBypassCode();

        assertThat(properties.getBypassCode()).isNull();
    }

    @Test
    @DisplayName("SEC-001: stage profile with empty bypass code keeps bypass disabled")
    void validateBypassCode_ShouldKeepNull_InStageWhenEnvNotSet() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("stage");
        OtpProperties properties = new OtpProperties(environment);
        // Simulates application-stage.yml: bypass-code: ${OTP_BYPASS_CODE:}
        // When OTP_BYPASS_CODE env is not set, Spring binds an empty string.
        properties.setBypassCode("");

        properties.validateBypassCode();

        // Empty bypass code must stay disabled — no default 123456 fallback.
        assertThat(properties.getBypassCode()).isEmpty();
    }

    @Test
    @DisplayName("SEC-001: dev profile with null bypass code keeps bypass disabled")
    void validateBypassCode_ShouldKeepNull_InDevWhenEnvNotSet() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        OtpProperties properties = new OtpProperties(environment);
        properties.setBypassCode(null);

        properties.validateBypassCode();

        assertThat(properties.getBypassCode()).isNull();
    }
}
