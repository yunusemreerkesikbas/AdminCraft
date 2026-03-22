package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.core.env.Profiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.TenantModule;
import com.backend.domain.enums.TenantStatus;
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

        @Mock
        private Environment environment;

        @Mock
        private TenantModuleRegistrar tenantModuleRegistrar;

        @Mock
        private ProvisioningJob jobMock;

        private AsyncProvisioningExecutor executor;

        private Long testTenantId;

        @BeforeEach
        void setUp() {
                executor = new AsyncProvisioningExecutor(
                                tenantRepository,
                                jobRepository,
                                tenantModuleRepository,
                                tenantMigrationService,
                                tenantModuleRegistrar,
                                environment);

                ReflectionTestUtils.setField(executor, "dbHost", "localhost");
                ReflectionTestUtils.setField(executor, "dbPort", "3306");
                ReflectionTestUtils.setField(executor, "dbUsername", "root");
                ReflectionTestUtils.setField(executor, "dbPassword", "test");

                testTenantId = 1L;
        }

        @Test
        void executeProvisioning_setsJobToFailed_whenDatabaseCreationFails() {
                // DB creation will fail in unit tests (no real MySQL) — verify error path
                List<String> modules = Arrays.asList("core", "pagebuilder");
		List<String> registeredModules = List.of("core");
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
                when(jobRepository.findById(1L)).thenReturn(Optional.of(jobMock));

		executor.executeProvisioning(1L, testTenantId, "ac_test_10001", modules, registeredModules, "corr-001");

                verify(jobMock).setStatus(eq("failed"));
                verify(tenantModuleRegistrar, never()).registerModules(any(), any());
        }

        @Test
        void executeProvisioning_doesNotCallMigration_whenDatabaseCreationFails() {
                List<String> modules = List.of("core");
		List<String> registeredModules = List.of("core");
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
                when(jobRepository.findById(1L)).thenReturn(Optional.of(jobMock));

		executor.executeProvisioning(1L, testTenantId, "ac_test_10001", modules, registeredModules, "corr-002");

                verify(tenantMigrationService, never()).migrateTenant(any(), any());
        }

        @Test
        void canRecreateDatabase_returnsTrue_whenPendingTenantNoModules_andDevProfile() {
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
                Tenant tenant = new Tenant();
                tenant.setStatus(TenantStatus.PENDING);
                when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(tenant));
                when(tenantModuleRepository.findByTenantId(testTenantId)).thenReturn(List.of());

                boolean result = ReflectionTestUtils.invokeMethod(executor, "canRecreateDatabase", testTenantId);

                assertThat(result).isTrue();
        }

        @Test
        void canRecreateDatabase_returnsFalse_whenTenantHasModules() {
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
                Tenant tenant = new Tenant();
                tenant.setStatus(TenantStatus.PENDING);
                when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(tenant));
                when(tenantModuleRepository.findByTenantId(testTenantId))
                                .thenReturn(List.of(TenantModule.builder().tenantId(testTenantId).moduleCode("core").build()));

                boolean result = ReflectionTestUtils.invokeMethod(executor, "canRecreateDatabase", testTenantId);

                assertThat(result).isFalse();
        }

        @Test
        void canRecreateDatabase_returnsFalse_whenTenantNotPending() {
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
                Tenant tenant = new Tenant();
                tenant.setStatus(TenantStatus.ACTIVE);
                when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(tenant));
                when(tenantModuleRepository.findByTenantId(testTenantId)).thenReturn(List.of());

                boolean result = ReflectionTestUtils.invokeMethod(executor, "canRecreateDatabase", testTenantId);

                assertThat(result).isFalse();
        }

        @Test
        void canRecreateDatabase_returnsFalse_whenNotDevOrTestProfile() {
                lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

                boolean result = ReflectionTestUtils.invokeMethod(executor, "canRecreateDatabase", testTenantId);

                assertThat(result).isFalse();
        }

        @Test
        void validateDatabaseName_throwsForInvalidName() {
                assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(executor, "validateDatabaseName", "invalid_db"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Invalid database name");
        }

        @Test
        void validateDatabaseName_throwsForNull() {
                assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(executor, "validateDatabaseName", new Object[] { null }))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Invalid database name");
        }

        @Test
        void validateDatabaseName_acceptsValidName() {
                ReflectionTestUtils.invokeMethod(executor, "validateDatabaseName", "ac_tenant_1");
                ReflectionTestUtils.invokeMethod(executor, "validateDatabaseName", "ac_tenant_123");
        }
}
