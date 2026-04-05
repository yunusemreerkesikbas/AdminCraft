package com.backend.infrastructure.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.application.service.TenantMigrationService;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

@ExtendWith(MockitoExtension.class)
class TenantStartupMigratorTest {

  @Mock
  private TenantPlatformRepository tenantRepository;

  @Mock
  private TenantModuleRepository tenantModuleRepository;

  @Mock
  private TenantMigrationService tenantMigrationService;

  private TenantStartupMigrator tenantStartupMigrator;

  @BeforeEach
  void setUp() {
    tenantStartupMigrator = new TenantStartupMigrator(tenantRepository, tenantModuleRepository, tenantMigrationService);
    ReflectionTestUtils.setField(tenantStartupMigrator, "autoSyncOnStartup", true);
  }

  @Test
  void shouldExpandCoreToRuntimeModulesDuringStartupMigration() throws Exception {
    Tenant tenant = Tenant.builder()
        .id(7L)
        .databaseName("ac_store_7")
        .status("active")
        .build();

    when(tenantRepository.findAll()).thenReturn(List.of(tenant));
    when(tenantModuleRepository.findByTenantIdAndStatus(7L, "enabled")).thenReturn(List.of(
        TenantModule.builder().tenantId(7L).moduleCode("core").status("enabled").build(),
        TenantModule.builder().tenantId(7L).moduleCode("product").status("enabled").build()));

    tenantStartupMigrator.run();

    verify(tenantMigrationService).migrateTenant("ac_store_7",
        List.of("core", "media", "component_library", "pagebuilder", "product"));
  }
}
