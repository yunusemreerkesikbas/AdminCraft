package com.backend.infrastructure.web;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.TenantMigrationService;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantStartupMigrator implements CommandLineRunner {

  private final TenantPlatformRepository tenantRepository;
  private final TenantModuleRepository tenantModuleRepository;
  private final TenantMigrationService tenantMigrationService;

  @Override
  @Transactional(readOnly = true)
  public void run(String... args) throws Exception {
    log.info("Checking for tenant database migrations...");

    List<Tenant> tenants = tenantRepository.findAll();
    for (Tenant tenant : tenants) {
      try {
        if ("active".equalsIgnoreCase(tenant.getStatus())) {
          migrateTenant(tenant);
        }
      } catch (Exception e) {
        log.error("Failed to migrate tenant: {}", tenant.getId(), e);
        // Continue with other tenants, don't block startup completely
      }
    }
  }

  private void migrateTenant(Tenant tenant) {
    List<TenantModule> modules = tenantModuleRepository.findByTenantId(tenant.getId());
    List<String> moduleCodes = modules.stream()
        .map(TenantModule::getModuleCode)
        .toList();

    log.info("Migrating tenant {} (DB: {}) with modules: {}", tenant.getId(), tenant.getDatabaseName(), moduleCodes);
    tenantMigrationService.migrateTenant(tenant.getDatabaseName(), moduleCodes);
  }
}
