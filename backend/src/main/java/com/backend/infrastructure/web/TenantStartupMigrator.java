package com.backend.infrastructure.web;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.TenantMigrationService;
import com.backend.domain.enums.ModuleCode;
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

  @Value("${app.tenant.auto-sync-on-startup:true}")
  private boolean autoSyncOnStartup;

  @Override
  @Transactional(readOnly = true)
  public void run(String... args) throws Exception {
    if (!autoSyncOnStartup) {
      log.info("Tenant auto-sync on startup is disabled (app.tenant.auto-sync-on-startup=false)");
      return;
    }

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
    List<TenantModule> modules = tenantModuleRepository.findByTenantIdAndStatus(tenant.getId(), "enabled");
    List<String> installedModuleCodes = modules.stream()
        .map(TenantModule::getModuleCode)
        .filter(Objects::nonNull)
        .map(ModuleCode::normalize)
        .filter(code -> {
          if (!ModuleCode.isValidCode(code)) {
            log.warn("Ignoring unsupported tenant module '{}' during startup migration for tenant {}", code,
                tenant.getId());
            return false;
          }
          return true;
        })
        .toList();

    List<String> runtimeModuleCodes = ModuleCode.resolveExecutionCodes(installedModuleCodes);

    log.info("Migrating tenant {} (DB: {}) with installedModules={} runtimeModules={}",
        tenant.getId(), tenant.getDatabaseName(), installedModuleCodes, runtimeModuleCodes);
    tenantMigrationService.migrateTenant(tenant.getDatabaseName(), runtimeModuleCodes);
  }
}
