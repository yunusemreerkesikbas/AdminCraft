package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
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

@ExtendWith(MockitoExtension.class)
class ConfigGlobalPropertiesAdminServiceImplTest {

    @Mock
    private PlatformConfigPropertyRepository propertyRepository;

    @Mock
    private ConfigChangeAuditRepository auditRepository;

    @Mock
    private EncryptionServicePort encryptionService;

    private ConfigGlobalPropertiesAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfigGlobalPropertiesAdminServiceImpl(
                propertyRepository,
                auditRepository,
                new ObjectMapper(),
                new MockEnvironment(),
                encryptionService);
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

    private ConfigPrincipal superAdminPrincipal() {
        return new ConfigPrincipal(
                1L,
                "super-admin@craftive.test",
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                null);
    }
}
