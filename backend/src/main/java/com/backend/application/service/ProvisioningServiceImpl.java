package com.backend.application.service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.dto.provisioning.SyncMigrationsRequest;
import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.TenantModule;
import com.backend.domain.repository.ProvisioningJobRepository;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

  private static final String CORE_MODULE = "core";
  private static final List<String> CORE_EXECUTION_MODULES = List.of(
      "media",
      "component_library",
      "pagebuilder");

  private final TenantRepository tenantRepository;
  private final TenantModuleRepository tenantModuleRepository;
  private final ProvisioningJobRepository jobRepository;
  private final ObjectMapper objectMapper;
  private final AsyncProvisioningExecutor asyncExecutor;
  private final TenantMigrationService migrationService;

  public ProvisioningServiceImpl(TenantRepository tenantRepository,
      TenantModuleRepository tenantModuleRepository,
      ProvisioningJobRepository jobRepository,
      ObjectMapper objectMapper,
      AsyncProvisioningExecutor asyncExecutor,
      TenantMigrationService migrationService) {
    this.tenantRepository = tenantRepository;
    this.tenantModuleRepository = tenantModuleRepository;
    this.jobRepository = jobRepository;
    this.objectMapper = objectMapper;
    this.asyncExecutor = asyncExecutor;
    this.migrationService = migrationService;
  }

  @Override
  @Transactional("platformTransactionManager")
  public ProvisioningJobResponse provisionTenant(Long tenantId, ProvisionRequest request) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

    List<String> resolvedModules = resolveProvisioningModules(request.getModules());

    List<String> orderedModules = migrationService.getOrderedModules(resolvedModules);
    log.info("Modules reordered for tenant {}: requested={}, resolved={}, ordered={}",
        tenantId, request.getModules(), resolvedModules, orderedModules);

    String correlationId = UUID.randomUUID().toString();

    try {
      String payload = objectMapper.writeValueAsString(request);

      ProvisioningJob job = ProvisioningJob.builder()
          .tenantId(tenantId)
          .type("full-provision")
          .payload(payload)
          .status("pending")
          .progress(0)
          .correlationId(correlationId)
          .build();

      job = jobRepository.save(job);

      String dbName = tenant.getDatabaseName();
      if (dbName == null || dbName.isEmpty() || (dbName.startsWith("tenant_") && dbName.endsWith("_db"))) {
        dbName = Tenant.formatDatabaseName(tenant.getSubdomain(), tenant.getId());
        tenant.setDatabaseName(dbName);
        tenantRepository.save(tenant);
      }

      final Long fJobId = job.getId();
      final String fDbName = dbName;
      final java.util.List<String> fModules = orderedModules;
      final String fCorrelationId = correlationId;

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            asyncExecutor.executeProvisioning(fJobId, tenantId, fDbName, fModules, fCorrelationId);
          }
        });
      } else {
        asyncExecutor.executeProvisioning(fJobId, tenantId, fDbName, fModules, fCorrelationId);
      }

      return mapToResponse(job);

    } catch (Exception e) {
      log.error("Failed to create provisioning job for tenant {}", tenantId, e);
      throw new RuntimeException("Failed to create provisioning job", e);
    }
  }

  @Override
  @Transactional("platformTransactionManager")
  public ProvisioningJobResponse syncTenantMigrations(Long tenantId, SyncMigrationsRequest request) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    List<String> installedModules = tenantModuleRepository.findByTenantId(tenantId)
        .stream()
        .map(TenantModule::getModuleCode)
        .toList();

    if (installedModules.isEmpty()) {
      throw new IllegalStateException("Tenant has no installed modules. Use full provision instead.");
    }
    List<String> modulesToSync = (request != null && request.getModules() != null && !request.getModules().isEmpty())
        ? request.getModules()
        : installedModules;
    Set<String> installedNormalized = normalizeInstalledModuleCodes(installedModules);
    boolean hasCoreInstalled = installedNormalized.contains(CORE_MODULE);
    for (String module : modulesToSync) {
      String normalized = normalizeAndValidateModuleCode(module);
      boolean installed = installedNormalized.contains(normalized);
      boolean coveredByCore = hasCoreInstalled && isCoreCoveredModule(normalized);
      if (!installed && !coveredByCore) {
        throw new IllegalArgumentException("Module not installed: " + module);
      }
    }

    List<String> resolvedModules = resolveSyncModules(modulesToSync);

    List<String> orderedModules = migrationService.getOrderedModules(resolvedModules);
    log.info("Modules reordered for sync migrations tenant {}: requested={}, resolved={}, ordered={}",
        tenantId, modulesToSync, resolvedModules, orderedModules);

    String correlationId = UUID.randomUUID().toString();
    try {
      String payload = objectMapper.writeValueAsString(request);
      ProvisioningJob job = ProvisioningJob.builder()
          .tenantId(tenantId)
          .type("sync-migrations")
          .payload(payload)
          .status("pending")
          .progress(0)
          .correlationId(correlationId)
          .build();

      job = jobRepository.save(job);

      String dbName = tenant.getDatabaseName();
      if (dbName == null || dbName.isEmpty() || (dbName.startsWith("tenant_") && dbName.endsWith("_db"))) {
        dbName = Tenant.formatDatabaseName(tenant.getSubdomain(), tenant.getId());
        tenant.setDatabaseName(dbName);
        tenantRepository.save(tenant);
      }

      final Long fJobId = job.getId();
      final String fDbName = dbName;
      final List<String> fModules = orderedModules;
      final String fCorrelationId = correlationId;
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            asyncExecutor.executeSyncMigrations(fJobId, tenantId, fDbName, fModules, fCorrelationId);
          }
        });
      } else {
        asyncExecutor.executeSyncMigrations(fJobId, tenantId, fDbName, fModules, fCorrelationId);
      }
      return mapToResponse(job);
    } catch (Exception e) {
      log.error("Failed to create sync migrations job for tenant {}", tenantId, e);
      throw new RuntimeException("Failed to create sync migrations job", e);
    }
  }

  @Override
  public ProvisioningJobResponse getJobStatus(Long jobId) {
    ProvisioningJob job = jobRepository.findById(jobId)
        .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    return mapToResponse(job);
  }

  private List<String> resolveProvisioningModules(List<String> moduleCodes) {
    if (moduleCodes == null || moduleCodes.isEmpty()) {
      throw new IllegalArgumentException("At least one module must be selected");
    }
    List<String> resolved = resolveSyncModules(moduleCodes);
    if (!resolved.contains(CORE_MODULE)) {
      throw new IllegalArgumentException("Core module is required");
    }
    return resolved;
  }

  private List<String> resolveSyncModules(List<String> moduleCodes) {
    if (moduleCodes == null || moduleCodes.isEmpty()) {
      return List.of();
    }
    LinkedHashSet<String> resolved = new LinkedHashSet<>();
    for (String rawCode : moduleCodes) {
      String code = normalizeAndValidateModuleCode(rawCode);
      if (CORE_MODULE.equals(code)) {
        addCoreExecutionModules(resolved);
      } else if (CORE_EXECUTION_MODULES.contains(code)) {
        resolved.add(code);
      } else {
        resolved.add(code);
      }
    }
    return List.copyOf(resolved);
  }

  private Set<String> normalizeInstalledModuleCodes(List<String> installedModules) {
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    if (installedModules == null) {
      return normalized;
    }
    for (String module : installedModules) {
      if (module == null || module.isBlank()) {
        continue;
      }
      try {
        normalized.add(normalizeAndValidateModuleCode(module));
      } catch (IllegalArgumentException ex) {
        log.warn("Ignoring unsupported installed module code '{}' during sync validation", module);
      }
    }
    return normalized;
  }

  private boolean isCoreCoveredModule(String code) {
    return CORE_MODULE.equals(code) || CORE_EXECUTION_MODULES.contains(code);
  }

  private void addCoreExecutionModules(LinkedHashSet<String> resolved) {
    resolved.add(CORE_MODULE);
    resolved.addAll(CORE_EXECUTION_MODULES);
  }

  private String normalizeAndValidateModuleCode(String rawCode) {
    String normalized = rawCode == null ? "" : rawCode.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) {
      throw new IllegalArgumentException("Invalid module code: " + rawCode);
    }
    if (!com.backend.domain.enums.ModuleCode.isValidCode(normalized)) {
      throw new IllegalArgumentException("Invalid module code: " + rawCode);
    }
    return normalized;
  }

  private ProvisioningJobResponse mapToResponse(ProvisioningJob job) {
    return ProvisioningJobResponse.builder()
        .jobId(job.getId())
        .tenantId(job.getTenantId())
        .type(job.getType())
        .status(job.getStatus())
        .progress(job.getProgress())
        .error(job.getError())
        .createdAt(job.getCreatedAt())
        .startedAt(job.getStartedAt())
        .completedAt(job.getCompletedAt())
        .build();
  }
}
