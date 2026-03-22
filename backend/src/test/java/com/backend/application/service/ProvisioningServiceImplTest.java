package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.ProvisioningJobRepository;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceImplTest {

  @Mock
  private TenantRepository tenantRepository;

  @Mock
  private TenantModuleRepository tenantModuleRepository;

  @Mock
  private ProvisioningJobRepository jobRepository;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private AsyncProvisioningExecutor asyncExecutor;

  @Mock
  private TenantMigrationService migrationService;

  private ProvisioningServiceImpl provisioningService;

  private Tenant tenant;

  @BeforeEach
  void setUp() {
    provisioningService = new ProvisioningServiceImpl(
        tenantRepository,
        tenantModuleRepository,
        jobRepository,
        objectMapper,
        asyncExecutor,
        migrationService);

    tenant = new Tenant();
    tenant.setId(1L);
    tenant.setSubdomain("tenant-a");
    tenant.setCompanyName("Tenant A");
    tenant.setDatabaseName("ac_tenant_1");
    tenant.setStatus(TenantStatus.PENDING);

    when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
  }

  private void stubProvisioningFlow() throws Exception {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    when(jobRepository.save(any(ProvisioningJob.class))).thenAnswer(invocation -> {
      ProvisioningJob job = invocation.getArgument(0);
      job.setId(42L);
      return job;
    });
    when(migrationService.getOrderedModules(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void shouldExpandCoreProvisioningToCoreExecutionModules() throws Exception {
    stubProvisioningFlow();
    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("core"))
        .build();

    provisioningService.provisionTenant(1L, request);

    List<String> expected = List.of("core", "media", "component_library", "pagebuilder");
    verify(migrationService).getOrderedModules(eq(expected));
    verify(asyncExecutor).executeProvisioning(eq(42L), eq(1L), eq(Tenant.formatDatabaseName("tenant-a", 1L)),
	eq(expected), eq(List.of("core")), anyString());
  }

  @Test
  void shouldIncludeProductWhenRequested() throws Exception {
    stubProvisioningFlow();
    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("core", "product"))
        .build();

    provisioningService.provisionTenant(1L, request);

    List<String> expected = List.of("core", "media", "component_library", "pagebuilder", "product");
    verify(migrationService).getOrderedModules(eq(expected));
    verify(asyncExecutor).executeProvisioning(eq(42L), eq(1L), eq(Tenant.formatDatabaseName("tenant-a", 1L)),
	eq(expected), eq(List.of("core", "product")), anyString());
  }

  @Test
  void shouldRejectProvisioningWithoutCore() {
    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("product"))
        .build();

    assertThatThrownBy(() -> provisioningService.provisionTenant(1L, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Core module is required");
  }

  @Test
  void shouldRejectCoreExecutionModulesInProvisioningRequest() {
    ProvisionRequest request = ProvisionRequest.builder()
	.modules(List.of("core", "media"))
	.build();

    assertThatThrownBy(() -> provisioningService.provisionTenant(1L, request))
	.isInstanceOf(IllegalArgumentException.class)
	.hasMessageContaining("Invalid provisioning module code");
  }
}
