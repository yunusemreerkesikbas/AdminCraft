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
}
