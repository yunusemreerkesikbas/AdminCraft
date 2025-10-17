package com.backend.application.service;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.infrastructure.persistence.platform.entity.ModuleCatalog;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.ModuleCatalogRepository;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

  private final TenantPlatformRepository tenantRepository;
  private final ModuleCatalogRepository moduleCatalogRepository;
  private final ProvisioningJobRepository jobRepository;
  private final ObjectMapper objectMapper;
  private final AsyncProvisioningExecutor asyncExecutor;

  public ProvisioningServiceImpl(TenantPlatformRepository tenantRepository,
      ModuleCatalogRepository moduleCatalogRepository,
      ProvisioningJobRepository jobRepository,
      ObjectMapper objectMapper,
      AsyncProvisioningExecutor asyncExecutor) {
    this.tenantRepository = tenantRepository;
    this.moduleCatalogRepository = moduleCatalogRepository;
    this.jobRepository = jobRepository;
    this.objectMapper = objectMapper;
    this.asyncExecutor = asyncExecutor;
  }

  @Override
  @Transactional("platformTransactionManager")
  public ProvisioningJobResponse provisionTenant(Long tenantId, ProvisionRequest request) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

    validateModules(request.getModules());

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

      asyncExecutor.executeProvisioning(job.getId(), tenant, request.getModules(), correlationId);

      return mapToResponse(job);

    } catch (Exception e) {
      log.error("Failed to create provisioning job for tenant {}", tenantId, e);
      throw new RuntimeException("Failed to create provisioning job", e);
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

