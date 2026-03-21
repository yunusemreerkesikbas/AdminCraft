package com.backend.presentation;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ProvisioningController {

  private final ProvisioningService provisioningService;
  private final ModuleCatalogService moduleCatalogService;
  private final MessageSource messageSource;

  // Rate limiter: 20 requests per minute per tenant
  private final ConcurrentHashMap<Long, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
  private static final double PERMITS_PER_MINUTE = 20.0;
  private static final double PERMITS_PER_SECOND = PERMITS_PER_MINUTE / 60.0;

  public ProvisioningController(ProvisioningService provisioningService,
      ModuleCatalogService moduleCatalogService,
      MessageSource messageSource) {
    this.provisioningService = provisioningService;
    this.moduleCatalogService = moduleCatalogService;
    this.messageSource = messageSource;
  }

  private String message(String key, Object[] args, String languageCode) {
    return messageSource.getMessage(key, args, Locale.forLanguageTag(languageCode));
  }

  private RateLimiter getRateLimiter(Long tenantId) {
    return rateLimiters.computeIfAbsent(tenantId,
        id -> RateLimiter.create(PERMITS_PER_SECOND));
  }

  private <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, String messageKey, String languageCode, T data) {
    return ResponseEntity.status(status).body(new ApiResponse<>("SUCCESS",
        message(messageKey, null, languageCode), data));
  }

  private <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String languageCode, String messageKey, T data) {
    return ResponseEntity.status(status).body(new ApiResponse<>("ERROR",
        message(messageKey, null, languageCode), data));
  }

  @PostMapping("/tenants/{tenantId}/provision")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> provisionTenant(
      @PathVariable Long tenantId,
      @Valid @RequestBody ProvisionRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {

    RateLimiter rateLimiter = getRateLimiter(tenantId);
    if (!rateLimiter.tryAcquire()) {
      log.warn("Rate limit exceeded for tenant {}", tenantId);
      return error(HttpStatus.TOO_MANY_REQUESTS, languageCode, "provisioning.rate.limit.exceeded", null);
    }

    try {
      log.info("Provisioning request for tenant {} with modules: {}", tenantId, request.getModules());

      ProvisioningJobResponse response = provisioningService.provisionTenant(tenantId, request);
      return success(HttpStatus.OK, "provisioning.job.started", languageCode, response);

    } catch (IllegalArgumentException e) {
      log.warn("Invalid provisioning request for tenant {}: {}", tenantId, e.getMessage());
      return error(HttpStatus.BAD_REQUEST, languageCode, "error.invalid.data", null);
    } catch (Exception e) {
      log.error("Provisioning request failed for tenant {}", tenantId, e);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, languageCode, "provisioning.start.failed", null);
    }
  }

  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> getJobStatus(
      @PathVariable Long jobId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      ProvisioningJobResponse response = provisioningService.getJobStatus(jobId);
      return success(HttpStatus.OK, "provisioning.job.status.retrieved", languageCode, response);

    } catch (IllegalArgumentException e) {
      log.warn("Job status request failed for jobId {}: {}", jobId, e.getMessage());
      return error(HttpStatus.BAD_REQUEST, languageCode, "error.invalid.data", null);
    } catch (Exception e) {
      log.error("Failed to get job status for jobId {}", jobId, e);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, languageCode, "provisioning.job.retrieve.failed", null);
    }
  }

  @PostMapping("/tenants/{tenantId}/sync-migrations")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> syncMigrations(
      @PathVariable Long tenantId,
      @RequestBody(required = false) SyncMigrationsRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    RateLimiter rateLimiter = getRateLimiter(tenantId);
    if (!rateLimiter.tryAcquire()) {
      log.warn("Rate limit exceeded for tenant {} on sync-migrations", tenantId);
      return error(HttpStatus.TOO_MANY_REQUESTS, languageCode, "provisioning.sync.rate.limit.exceeded", null);
    }

    try {
      log.info("Sync migrations request for tenant {}", tenantId);

      if (request == null) {
        request = new SyncMigrationsRequest();
      }

      ProvisioningJobResponse response = provisioningService.syncTenantMigrations(tenantId, request);
      return success(HttpStatus.OK, "provisioning.sync.job.started", languageCode, response);

    } catch (IllegalArgumentException | IllegalStateException e) {
      log.warn("Invalid sync migrations request for tenant {}: {}", tenantId, e.getMessage());
      int errorCode = "Tenant has no installed modules. Use full provision instead.".equals(e.getMessage())
          ? 4001
          : HttpStatus.BAD_REQUEST.value();
      return ResponseEntity.badRequest().body(new ApiResponse<>("ERROR",
          message("error.invalid.data", null, languageCode), null, errorCode));
    } catch (Exception e) {
      log.error("Sync migrations failed for tenant {}", tenantId, e);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, languageCode, "provisioning.sync.start.failed", null);
    }
  }

  @GetMapping("/modules/catalog")
  public ResponseEntity<ApiResponse<List<ModuleCatalogResponse>>> getModulesCatalog(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<ModuleCatalogResponse> modules = moduleCatalogService.getAllModules();
      return success(HttpStatus.OK, "provisioning.modules.catalog.retrieved", languageCode, modules);

    } catch (Exception e) {
      log.error("Failed to get modules catalog", e);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, languageCode, "provisioning.modules.catalog.failed", null);
    }
  }
}
