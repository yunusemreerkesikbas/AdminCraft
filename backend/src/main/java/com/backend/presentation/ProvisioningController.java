package com.backend.presentation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.provisioning.ModuleCatalogResponse;
import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.dto.provisioning.SyncMigrationsRequest;
import com.backend.application.service.ModuleCatalogService;
import com.backend.application.service.ProvisioningService;
import com.backend.shared.common.ApiResponse;
import com.google.common.util.concurrent.RateLimiter;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/provisioning")
public class ProvisioningController {

  private final ProvisioningService provisioningService;
  private final ModuleCatalogService moduleCatalogService;

  // Rate limiter: 5 requests per minute per tenant
  private final ConcurrentHashMap<Long, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
  private static final double PERMITS_PER_MINUTE = 5.0;
  private static final double PERMITS_PER_SECOND = PERMITS_PER_MINUTE / 60.0;

  public ProvisioningController(ProvisioningService provisioningService,
      ModuleCatalogService moduleCatalogService) {
    this.provisioningService = provisioningService;
    this.moduleCatalogService = moduleCatalogService;
  }

  /**
   * Get or create rate limiter for tenant
   */
  private RateLimiter getRateLimiter(Long tenantId) {
    return rateLimiters.computeIfAbsent(tenantId,
        id -> RateLimiter.create(PERMITS_PER_SECOND));
  }

  @PostMapping("/tenants/{tenantId}/provision")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> provisionTenant(
      @PathVariable Long tenantId,
      @Valid @RequestBody ProvisionRequest request) {

    // Rate limiting: 5 requests per minute per tenant
    RateLimiter rateLimiter = getRateLimiter(tenantId);
    if (!rateLimiter.tryAcquire()) {
      log.warn("Rate limit exceeded for tenant {}", tenantId);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(
          "ERROR",
          "Rate limit exceeded. Maximum 5 provisioning requests per minute.",
          null));
    }

    try {
      log.info("Provisioning request for tenant {} with modules: {}", tenantId, request.getModules());

      ProvisioningJobResponse response = provisioningService.provisionTenant(tenantId, request);

      return ResponseEntity.ok(new ApiResponse<>(
          "SUCCESS",
          "Provisioning job started",
          response));

    } catch (IllegalArgumentException e) {
      log.error("Invalid provisioning request for tenant {}: {}", tenantId, e.getMessage());
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          "ERROR",
          e.getMessage(),
          null));
    } catch (Exception e) {
      log.error("Provisioning request failed for tenant {}", tenantId, e);
      return ResponseEntity.internalServerError().body(new ApiResponse<>(
          "ERROR",
          "Failed to start provisioning job",
          null));
    }
  }

  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> getJobStatus(@PathVariable Long jobId) {
    try {
      ProvisioningJobResponse response = provisioningService.getJobStatus(jobId);

      return ResponseEntity.ok(new ApiResponse<>(
          "SUCCESS",
          "Job status retrieved",
          response));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          "ERROR",
          e.getMessage(),
          null));
    } catch (Exception e) {
      log.error("Failed to get job status for jobId {}", jobId, e);
      return ResponseEntity.internalServerError().body(new ApiResponse<>(
          "ERROR",
          "Failed to retrieve job status",
          null));
    }
  }

  @PostMapping("/tenants/{tenantId}/sync-migrations")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> syncMigrations(
      @PathVariable Long tenantId,
      @RequestBody(required = false) SyncMigrationsRequest request) {
    RateLimiter rateLimiter = getRateLimiter(tenantId);
    if (!rateLimiter.tryAcquire()) {
      log.warn("Rate limit exceeded for tenant {} on sync-migrations", tenantId);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(
          "ERROR",
          "Rate limit exceeded. Maximum 5 requests per minute.",
          null));
    }

    try {
      log.info("Sync migrations request for tenant {}", tenantId);

      if (request == null) {
        request = new SyncMigrationsRequest();
      }

      ProvisioningJobResponse response = provisioningService.syncTenantMigrations(tenantId, request);

      return ResponseEntity.ok(new ApiResponse<>(
          "SUCCESS",
          "Migration sync job started",
          response));

    } catch (IllegalArgumentException | IllegalStateException e) {
      log.warn("Invalid sync migrations request for tenant {}: {}", tenantId, e.getMessage());
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          "ERROR",
          e.getMessage(),
          null));
    } catch (Exception e) {
      log.error("Sync migrations failed for tenant {}", tenantId, e);
      return ResponseEntity.internalServerError().body(new ApiResponse<>(
          "ERROR",
          "Failed to start sync migrations job",
          null));
    }
  }

  @GetMapping("/modules/catalog")
  public ResponseEntity<ApiResponse<List<ModuleCatalogResponse>>> getModulesCatalog() {
    try {
      List<ModuleCatalogResponse> modules = moduleCatalogService.getAllModules();

      return ResponseEntity.ok(new ApiResponse<>(
          "SUCCESS",
          "Modules catalog retrieved",
          modules));

    } catch (Exception e) {
      log.error("Failed to get modules catalog", e);
      return ResponseEntity.internalServerError().body(new ApiResponse<>(
          "ERROR",
          "Failed to retrieve modules catalog",
          null));
    }
  }
}
