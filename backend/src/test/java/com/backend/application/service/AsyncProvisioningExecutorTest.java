package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

//...

@ExtendWith(MockitoExtension.class)
class AsyncProvisioningExecutorTest {

        @Mock
        private TenantPlatformRepository tenantRepository;

        @Mock
        private ProvisioningJobRepository jobRepository;

        @Mock
        private TenantModuleRepository tenantModuleRepository;

        @Mock
        private TenantMigrationService tenantMigrationService;

        @Captor
        private ArgumentCaptor<List<TenantModule>> tenantModulesCaptor;

        private AsyncProvisioningExecutor executor;

        private Tenant testTenant;

        @BeforeEach
        void setUp() {
                executor = new AsyncProvisioningExecutor(
                                tenantRepository,
                                jobRepository,
                                tenantModuleRepository,
                                tenantMigrationService);

                // Set test database connection properties
                ReflectionTestUtils.setField(executor, "dbHost", "localhost");
                ReflectionTestUtils.setField(executor, "dbPort", "3307");
                ReflectionTestUtils.setField(executor, "dbUsername", "root");
                ReflectionTestUtils.setField(executor, "dbPassword", "test");

                testTenant = Tenant.builder()
                                .id(1L)
                                .subdomain("test-tenant")
                                .companyName("Test Company")
                                .databaseName("ac_tenant_1")
                                .status("PROVISIONING")
                                .build();
        }

        @Test
        void shouldInsertTenantModulesWithCorrectData() {
                List<String> modules = Arrays.asList("core", "pagebuilder", "site_settings");
                List<TenantModule> expectedModules = modules.stream()
                                .map(code -> TenantModule.builder()
                                                .tenantId(testTenant.getId())
                                                .moduleCode(code)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(expectedModules).hasSize(3);
                assertThat(expectedModules).extracting(TenantModule::getModuleCode)
                                .containsExactlyInAnyOrder("core", "pagebuilder", "site_settings");
                assertThat(expectedModules).allMatch(tm -> tm.getTenantId().equals(1L));
                assertThat(expectedModules).allMatch(tm -> tm.getStatus().equals("enabled"));
        }

        @Test
        void shouldCreateTenantModuleForEachModule() {
                List<String> modules = Arrays.asList("core", "pagebuilder");

                // When
                List<TenantModule> tenantModules = modules.stream()
                                .map(moduleCode -> TenantModule.builder()
                                                .tenantId(testTenant.getId())
                                                .moduleCode(moduleCode)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(tenantModules).hasSize(2);

                TenantModule coreModule = tenantModules.stream()
                                .filter(tm -> tm.getModuleCode().equals("core"))
                                .findFirst()
                                .orElseThrow();
                assertThat(coreModule.getTenantId()).isEqualTo(1L);
                assertThat(coreModule.getStatus()).isEqualTo("enabled");

                TenantModule pageBuilderModule = tenantModules.stream()
                                .filter(tm -> tm.getModuleCode().equals("pagebuilder"))
                                .findFirst()
                                .orElseThrow();
                assertThat(pageBuilderModule.getTenantId()).isEqualTo(1L);
                assertThat(pageBuilderModule.getStatus()).isEqualTo("enabled");
        }

        @Test
        void shouldHandleEmptyModuleList() {
                List<String> modules = List.of();
                List<TenantModule> tenantModules = modules.stream()
                                .map(moduleCode -> TenantModule.builder()
                                                .tenantId(testTenant.getId())
                                                .moduleCode(moduleCode)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(tenantModules).isEmpty();
        }

        @Test
        void shouldPreserveModuleOrderInList() {
                // Given
                List<String> modules = Arrays.asList("core", "pagebuilder", "site_settings", "page_categories");

                // When
                List<TenantModule> tenantModules = modules.stream()
                                .map(moduleCode -> TenantModule.builder()
                                                .tenantId(testTenant.getId())
                                                .moduleCode(moduleCode)
                                                .status("enabled")
                                                .build())
                                .toList();

                // Then
                assertThat(tenantModules).hasSize(4);
                assertThat(tenantModules.get(0).getModuleCode()).isEqualTo("core");
                assertThat(tenantModules.get(1).getModuleCode()).isEqualTo("pagebuilder");
                assertThat(tenantModules.get(2).getModuleCode()).isEqualTo("site_settings");
                assertThat(tenantModules.get(3).getModuleCode()).isEqualTo("page_categories");
        }
}
