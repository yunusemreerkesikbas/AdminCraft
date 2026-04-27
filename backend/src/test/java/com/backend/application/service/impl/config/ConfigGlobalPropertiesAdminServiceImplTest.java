package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigPropertyResult;
import com.backend.domain.entity.PlatformConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.repository.PlatformConfigPropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class ConfigGlobalPropertiesAdminServiceImplTest {

    @Mock
    private PlatformConfigPropertyRepository propertyRepository;

    @Mock
    private ConfigChangeAuditRepository auditRepository;

    @Mock
    private EncryptionServicePort encryptionService;

    private SimpleMeterRegistry meterRegistry;

    private ConfigGlobalPropertiesAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new ConfigGlobalPropertiesAdminServiceImpl(
                propertyRepository,
                auditRepository,
                new ObjectMapper(),
                new MockEnvironment(),
                encryptionService,
                meterRegistry);
    }

    @Test
    @DisplayName("listProperties should include GA4 enabled key with default false")
    void listProperties_ShouldIncludeGa4EnabledDefault() {
        List<ConfigPropertyResult> result = service.listProperties(superAdminPrincipal());

        assertThat(result).extracting(ConfigPropertyResult::key)
                .contains("platform.analytics.ga4.enabled", "platform.seo.insights.enabled");
        assertThat(result.stream()
                .filter(item -> "platform.analytics.ga4.enabled".equals(item.key()))
                .findFirst())
                .get()
                .extracting(ConfigPropertyResult::value, ConfigPropertyResult::secret)
                .containsExactly("false", false);
        assertThat(result.stream()
                .filter(item -> "platform.seo.insights.enabled".equals(item.key()))
                .findFirst())
                .get()
                .extracting(ConfigPropertyResult::value, ConfigPropertyResult::secret)
                .containsExactly("false", false);
    }

    @Test
    @DisplayName("getGa4AnalyticsEnabled should return true when overridden")
    void getGa4AnalyticsEnabled_ShouldReturnTrueWhenOverrideExists() {
        PlatformConfigProperty property = new PlatformConfigProperty();
        property.setConfigKey("platform.analytics.ga4.enabled");
        property.setConfigValue("true");

        when(propertyRepository.findByConfigKey("platform.analytics.ga4.enabled"))
                .thenReturn(Optional.of(property));

        assertThat(service.getGa4AnalyticsEnabled()).isTrue();
    }

    @Test
    @DisplayName("getSeoInsightsEnabled should return true when overridden")
    void getSeoInsightsEnabled_ShouldReturnTrueWhenOverrideExists() {
        PlatformConfigProperty property = new PlatformConfigProperty();
        property.setConfigKey("platform.seo.insights.enabled");
        property.setConfigValue("true");

        when(propertyRepository.findByConfigKey("platform.seo.insights.enabled"))
                .thenReturn(Optional.of(property));

        assertThat(service.getSeoInsightsEnabled()).isTrue();
    }

    @Test
    @DisplayName("listProperties should include OTP bypass keys with defaults")
    void listProperties_ShouldIncludeOtpBypassKeysWithDefaults() {
        List<ConfigPropertyResult> result = service.listProperties(superAdminPrincipal());

        assertThat(result).extracting(ConfigPropertyResult::key)
                .contains("platform.security.otp.bypass.enabled", "platform.security.otp.bypass.code");

        ConfigPropertyResult enabled = result.stream()
                .filter(item -> "platform.security.otp.bypass.enabled".equals(item.key()))
                .findFirst()
                .orElseThrow();
        assertThat(enabled.value()).isEqualTo("false");
        assertThat(enabled.secret()).isFalse();

        ConfigPropertyResult code = result.stream()
                .filter(item -> "platform.security.otp.bypass.code".equals(item.key()))
                .findFirst()
                .orElseThrow();
        assertThat(code.value()).isNull();
        assertThat(code.secret()).isTrue();
    }

    @Test
    @DisplayName("getOtpBypassEnabled should return true when override is true")
    void getOtpBypassEnabled_ShouldReturnTrueWhenOverrideTrue() {
        PlatformConfigProperty property = new PlatformConfigProperty();
        property.setConfigKey("platform.security.otp.bypass.enabled");
        property.setConfigValue("true");

        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.enabled"))
                .thenReturn(Optional.of(property));

        assertThat(service.getOtpBypassEnabled()).isTrue();
    }

    @Test
    @DisplayName("getOtpBypassEnabled should return false when no override exists")
    void getOtpBypassEnabled_ShouldReturnFalseWhenNoOverride() {
        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.enabled"))
                .thenReturn(Optional.empty());

        assertThat(service.getOtpBypassEnabled()).isFalse();
    }

    @Test
    @DisplayName("getOtpBypassCodeDecrypted should decrypt stored value when bypass enabled")
    void getOtpBypassCodeDecrypted_ShouldDecryptStoredValue() {
        PlatformConfigProperty enabled = new PlatformConfigProperty();
        enabled.setConfigKey("platform.security.otp.bypass.enabled");
        enabled.setConfigValue("true");
        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.enabled"))
                .thenReturn(Optional.of(enabled));

        PlatformConfigProperty property = new PlatformConfigProperty();
        property.setConfigKey("platform.security.otp.bypass.code");
        property.setConfigValue("ENC:xyz");

        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.code"))
                .thenReturn(Optional.of(property));
        when(encryptionService.decrypt("ENC:xyz")).thenReturn("plain-bypass-code");

        assertThat(service.getOtpBypassCodeDecrypted()).isEqualTo("plain-bypass-code");
    }

    @Test
    @DisplayName("getOtpBypassCodeDecrypted should return null when bypass disabled even if code exists")
    void getOtpBypassCodeDecrypted_ShouldReturnNullWhenBypassDisabled() {
        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.enabled"))
                .thenReturn(Optional.of(newDisabledBypassProperty()));

        assertThat(service.getOtpBypassCodeDecrypted()).isNull();
        verify(encryptionService, never()).decrypt(any());
    }

    @Test
    @DisplayName("getOtpBypassCodeDecrypted should return null when no override exists")
    void getOtpBypassCodeDecrypted_ShouldReturnNullWhenNoOverride() {
        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.enabled"))
                .thenReturn(Optional.empty());

        assertThat(service.getOtpBypassCodeDecrypted()).isNull();
    }

    @Test
    @DisplayName("upsertProperty should encrypt OTP bypass code value")
    void upsertProperty_ShouldEncryptOtpBypassCode() {
        when(propertyRepository.findByConfigKey("platform.security.otp.bypass.code"))
                .thenReturn(Optional.empty());
        when(encryptionService.encrypt("StrongBypassCode12345"))
                .thenReturn("ENC:super-secret-code");
        when(propertyRepository.save(any(PlatformConfigProperty.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.upsertProperty(superAdminPrincipal(),
                "platform.security.otp.bypass.code",
                "StrongBypassCode12345",
                true,
                "rotation");

        verify(encryptionService).encrypt("StrongBypassCode12345");
    }

    @Test
    @DisplayName("upsertProperty should reject OTP bypass code shorter than minimum")
    void upsertProperty_ShouldRejectShortOtpBypassCode() {
        assertThatThrownBy(() -> service.upsertProperty(superAdminPrincipal(),
                "platform.security.otp.bypass.code",
                "Short1Aa",
                true,
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP bypass code");

        verify(encryptionService, never()).encrypt(any());
    }

    @Test
    @DisplayName("upsertProperty should reject invalid OTP bypass enabled value")
    void upsertProperty_ShouldRejectInvalidOtpBypassEnabled() {
        assertThatThrownBy(() -> service.upsertProperty(superAdminPrincipal(),
                "platform.security.otp.bypass.enabled",
                "yes",
                false,
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP bypass enabled");

        verify(encryptionService, never()).encrypt(any());
    }

    @Test
    @DisplayName("upsertProperty should reject OTP bypass code without secret flag")
    void upsertProperty_ShouldRejectOtpBypassCodeWithoutSecretFlag() {
        assertThatThrownBy(() -> service.upsertProperty(superAdminPrincipal(),
                "platform.security.otp.bypass.code",
                "StrongBypassCode12345",
                false,
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secret flag must be true");

        verify(encryptionService, never()).encrypt(any());
    }

    private static PlatformConfigProperty newDisabledBypassProperty() {
        PlatformConfigProperty p = new PlatformConfigProperty();
        p.setConfigKey("platform.security.otp.bypass.enabled");
        p.setConfigValue("false");
        return p;
    }

    private ConfigPrincipal superAdminPrincipal() {
        return new ConfigPrincipal(
                1L,
                "super-admin@craftive.test",
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                null);
    }
}
