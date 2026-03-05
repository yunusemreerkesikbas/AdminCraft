package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class TenantDbExecutorTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantContext tenantContext;
    private TenantDbExecutor tenantDbExecutor;

    @BeforeEach
    void setUp() {
        tenantContext = new TenantContext(tenantRepository);
        tenantDbExecutor = new TenantDbExecutor(tenantContext);
    }

    @Test
    @DisplayName("withTenant should restore previous context when nested")
    void withTenant_ShouldRestoreContext_WhenNested() {
        tenantContext.setTenantId("1");
        tenantContext.setTenantDbName("ac_tenant_1");

        tenantDbExecutor.withTenant(2L, "ac_tenant_2", () -> {
            assertThat(tenantContext.getTenantId()).isEqualTo("2");
            assertThat(tenantContext.getTenantDbName()).isEqualTo("ac_tenant_2");

            tenantDbExecutor.withTenant(3L, "ac_tenant_3", () -> {
                assertThat(tenantContext.getTenantId()).isEqualTo("3");
                assertThat(tenantContext.getTenantDbName()).isEqualTo("ac_tenant_3");
                return null;
            });

            assertThat(tenantContext.getTenantId()).isEqualTo("2");
            assertThat(tenantContext.getTenantDbName()).isEqualTo("ac_tenant_2");
            return null;
        });

        assertThat(tenantContext.getTenantId()).isEqualTo("1");
        assertThat(tenantContext.getTenantDbName()).isEqualTo("ac_tenant_1");
    }

    @Test
    @DisplayName("withTenant should restore previous context even when action throws")
    void withTenant_ShouldRestoreContext_WhenActionThrows() {
        tenantContext.setTenantId("1");
        tenantContext.setTenantDbName("ac_tenant_1");

        assertThatThrownBy(() ->
                tenantDbExecutor.withTenant(2L, "ac_tenant_2", () -> {
                    throw new RuntimeException("simulated failure");
                })
        ).isInstanceOf(RuntimeException.class).hasMessage("simulated failure");

        assertThat(tenantContext.getTenantId()).isEqualTo("1");
        assertThat(tenantContext.getTenantDbName()).isEqualTo("ac_tenant_1");
    }
}

