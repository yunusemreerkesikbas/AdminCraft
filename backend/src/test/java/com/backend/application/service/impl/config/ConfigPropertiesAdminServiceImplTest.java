package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ConfigPropertyResult::key).containsExactly(
                "security.recaptcha.enabled",
                "security.recaptcha.site_key",
                "security.recaptcha.secret_key");
        assertThat(result.get(0).value()).isEqualTo("false");
        assertThat(result.get(1).value()).isNull();
        assertThat(result.get(2).value()).isNull();
        assertThat(result.get(2).secret()).isTrue();
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
