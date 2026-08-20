package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigPropertyResult;
import com.backend.application.service.TenantDbExecutor;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class ConfigPropertiesAdminServiceImplTest extends BaseServiceTest {

    private static final Long TEST_TENANT_ID_LONG = 1L;

    @Mock
    private ConfigPropertyService configPropertyService;

    @Mock
    private ConfigChangeAuditRepository auditRepository;

    private ConfigPropertiesAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfigPropertiesAdminServiceImpl(
                tenantRepository,
                new TenantDbExecutor(getTenantContext()),
                configPropertyService,
                auditRepository,
                new ObjectMapper());
    }

    @Test
    @DisplayName("listProperties should include managed recaptcha keys with config-store defaults when tenant overrides are empty")
    void listProperties_ShouldIncludeManagedDefaultRows() {
        Tenant tenant = tenantEntity();

        when(tenantRepository.findById(TEST_TENANT_ID_LONG)).thenReturn(Optional.of(tenant));
        when(configPropertyService.findAll(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME)).thenReturn(List.of());

        List<ConfigPropertyResult> result = service.listProperties(tenantPrincipal());

        assertThat(result).hasSize(8);
        assertThat(result).extracting(ConfigPropertyResult::key).containsExactly(
                "security.recaptcha.enabled",
                "security.recaptcha.site_key",
                "security.recaptcha.secret_key",
                "analytics.ga4.enabled",
                "analytics.ga4.property_id",
                "seo.insights.enabled",
                "seo.search_console.property_url",
                "security.otp.resend_cooldown_seconds");
        assertThat(result.get(0).value()).isEqualTo("false");
        assertThat(result.get(1).value()).isNull();
        assertThat(result.get(2).value()).isNull();
        assertThat(result.get(2).secret()).isTrue();
        assertThat(result.get(3).value()).isEqualTo("false");
        assertThat(result.get(3).secret()).isFalse();
        assertThat(result.get(4).value()).isNull();
        assertThat(result.get(4).secret()).isFalse();
        assertThat(result.get(5).value()).isEqualTo("false");
        assertThat(result.get(5).secret()).isFalse();
        assertThat(result.get(6).value()).isNull();
        assertThat(result.get(6).secret()).isFalse();
        assertThat(result.get(7).value()).isEqualTo("180");
        assertThat(result.get(7).secret()).isFalse();
    }

    @Test
    @DisplayName("upsertProperty should reject non boolean ga4 enabled values")
    void upsertProperty_ShouldRejectNonBooleanGa4EnabledValues() {
        when(tenantRepository.findById(TEST_TENANT_ID_LONG)).thenReturn(Optional.of(tenantEntity()));

        assertThatThrownBy(() -> service.upsertProperty(
                tenantPrincipal(),
                "analytics.ga4.enabled",
                "yes",
                false,
                "invalid test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GA4 enabled flag must be true or false");
    }

    @Test
    @DisplayName("upsertProperty should reject non numeric ga4 property ids")
    void upsertProperty_ShouldRejectNonNumericGa4PropertyIds() {
        when(tenantRepository.findById(TEST_TENANT_ID_LONG)).thenReturn(Optional.of(tenantEntity()));

        assertThatThrownBy(() -> service.upsertProperty(
                tenantPrincipal(),
                "analytics.ga4.property_id",
                "G-TEST123",
                false,
                "invalid test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GA4 property ID must be numeric");
    }

    @Test
    @DisplayName("upsertProperty should reject non boolean seo insights enabled values")
    void upsertProperty_ShouldRejectNonBooleanSeoInsightsEnabledValues() {
        when(tenantRepository.findById(TEST_TENANT_ID_LONG)).thenReturn(Optional.of(tenantEntity()));

        assertThatThrownBy(() -> service.upsertProperty(
                tenantPrincipal(),
                "seo.insights.enabled",
                "yes",
                false,
                "invalid test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SEO insights enabled flag must be true or false");
    }

    @Test
    @DisplayName("upsertProperty should reject invalid search console property urls")
    void upsertProperty_ShouldRejectInvalidSearchConsolePropertyUrls() {
        when(tenantRepository.findById(TEST_TENANT_ID_LONG)).thenReturn(Optional.of(tenantEntity()));

        assertThatThrownBy(() -> service.upsertProperty(
                tenantPrincipal(),
                "seo.search_console.property_url",
                "example.com",
                false,
                "invalid test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Search Console property URL must start with sc-domain: or http(s)://");
    }

    private ConfigPrincipal tenantPrincipal() {
        return new ConfigPrincipal(TEST_USER_ID, "tenant-admin@craftive.test", "CONFIG_TENANT_ADMIN", TEST_TENANT_ID_LONG);
    }

    private Tenant tenantEntity() {
        Tenant tenant = new Tenant();
        tenant.setId(TEST_TENANT_ID_LONG);
        tenant.setDatabaseName(TEST_TENANT_DB_NAME);
        tenant.setSubdomain("demo");
        return tenant;
    }
}
