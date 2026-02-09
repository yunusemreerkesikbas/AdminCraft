package com.backend.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.dto.provisioning.SyncMigrationsRequest;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.ModuleCatalogRepository;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

  private final TenantPlatformRepository tenantRepository;
  private final ModuleCatalogRepository moduleCatalogRepository;
  private final TenantModuleRepository tenantModuleRepository;
  private final ProvisioningJobRepository jobRepository;
  private final ObjectMapper objectMapper;
  private final AsyncProvisioningExecutor asyncExecutor;
  private final TenantMigrationService migrationService;

  public ProvisioningServiceImpl(TenantPlatformRepository tenantRepository,
      ModuleCatalogRepository moduleCatalogRepository,
      TenantModuleRepository tenantModuleRepository,
      ProvisioningJobRepository jobRepository,
      ObjectMapper objectMapper,
      AsyncProvisioningExecutor asyncExecutor,
      TenantMigrationService migrationService) {
    this.tenantRepository = tenantRepository;
    this.moduleCatalogRepository = moduleCatalogRepository;
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

    validateModules(request.getModules());

    // CRITICAL: Reorder modules according to FK dependencies
    List<String> orderedModules = migrationService.getOrderedModules(request.getModules());
    log.info("Modules reordered for tenant {}: {} -> {}", tenantId, request.getModules(), orderedModules);

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

      // Ensure async execution starts AFTER the surrounding transaction commits
      final Long fJobId = job.getId();
      final Tenant fTenant = tenant;
      final java.util.List<String> fModules = orderedModules;
      final String fCorrelationId = correlationId;

      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            asyncExecutor.executeProvisioning(fJobId, fTenant, fModules, fCorrelationId);
          }
        });
      } else {
        asyncExecutor.executeProvisioning(fJobId, fTenant, fModules, fCorrelationId);
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
    for (String module : modulesToSync) {
      if (!installedModules.contains(module)) {
        throw new IllegalArgumentException("Module not installed: " + module);
      }
    }

    // CRITICAL: Reorder modules according to FK dependencies
    List<String> orderedModules = migrationService.getOrderedModules(modulesToSync);
    log.info("Modules reordered for sync migrations tenant {}: {} -> {}", tenantId, modulesToSync, orderedModules);

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
      final Long fJobId = job.getId();
      final Tenant fTenant = tenant;
      final List<String> fModules = orderedModules;
      final String fCorrelationId = correlationId;
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            asyncExecutor.executeSyncMigrations(fJobId, fTenant, fModules, fCorrelationId);
          }
        });
      } else {
        asyncExecutor.executeSyncMigrations(fJobId, fTenant, fModules, fCorrelationId);
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

  private void validateModules(List<String> moduleCodes) {
    for (String code : moduleCodes) {
      moduleCatalogRepository.findByCode(code)
          .orElseThrow(() -> new IllegalArgumentException("Invalid module code: " + code));
    }

    if (!moduleCodes.contains("core")) {
      throw new IllegalArgumentException("Core module is required");
    }
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
