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

import com.backend.domain.entity.TenantModule;
import com.backend.domain.repository.ProvisioningJobRepository;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class AsyncProvisioningExecutorTest {

        @Mock
        private TenantRepository tenantRepository;

        @Mock
        private ProvisioningJobRepository jobRepository;

        @Mock
        private TenantModuleRepository tenantModuleRepository;

        @Mock
        private TenantMigrationService tenantMigrationService;

        @Captor
        private ArgumentCaptor<List<TenantModule>> tenantModulesCaptor;

        private AsyncProvisioningExecutor executor;

        private Long testTenantId;

        @BeforeEach
        void setUp() {
                executor = new AsyncProvisioningExecutor(
                                tenantRepository,
                                jobRepository,
                                tenantModuleRepository,
                                tenantMigrationService);

                ReflectionTestUtils.setField(executor, "dbHost", "localhost");
                ReflectionTestUtils.setField(executor, "dbPort", "3306");
                ReflectionTestUtils.setField(executor, "dbUsername", "root");
                ReflectionTestUtils.setField(executor, "dbPassword", "test");

                testTenantId = 1L;
        }

        @Test
        void shouldInsertTenantModulesWithCorrectData() {
                List<String> modules = Arrays.asList("core", "pagebuilder", "media");
                List<TenantModule> expectedModules = modules.stream()
                                .map(code -> TenantModule.builder()
                                                .tenantId(testTenantId)
                                                .moduleCode(code)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(expectedModules).hasSize(3);
                assertThat(expectedModules).extracting(TenantModule::getModuleCode)
                                .containsExactlyInAnyOrder("core", "pagebuilder", "media");
                assertThat(expectedModules).allMatch(tm -> tm.getTenantId().equals(1L));
                assertThat(expectedModules).allMatch(tm -> tm.getStatus().equals("enabled"));
        }

        @Test
        void shouldCreateTenantModuleForEachModule() {
                List<String> modules = Arrays.asList("core", "pagebuilder");

                List<TenantModule> tenantModules = modules.stream()
                                .map(moduleCode -> TenantModule.builder()
                                                .tenantId(testTenantId)
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
                                                .tenantId(testTenantId)
                                                .moduleCode(moduleCode)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(tenantModules).isEmpty();
        }

        @Test
        void shouldPreserveModuleOrderInList() {
                List<String> modules = Arrays.asList("core", "pagebuilder", "media");

                List<TenantModule> tenantModules = modules.stream()
                                .map(moduleCode -> TenantModule.builder()
                                                .tenantId(testTenantId)
                                                .moduleCode(moduleCode)
                                                .status("enabled")
                                                .build())
                                .toList();

                assertThat(tenantModules).hasSize(3);
                assertThat(tenantModules.get(0).getModuleCode()).isEqualTo("core");
                assertThat(tenantModules.get(1).getModuleCode()).isEqualTo("pagebuilder");
                assertThat(tenantModules.get(2).getModuleCode()).isEqualTo("media");
        }
}
