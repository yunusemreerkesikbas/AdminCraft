package com.backend.presentation;

import com.backend.application.dto.provisioning.ModuleCatalogResponse;
import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.service.ModuleCatalogService;
import com.backend.application.service.ProvisioningService;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/provisioning")
public class ProvisioningController {

  private final ProvisioningService provisioningService;
  private final ModuleCatalogService moduleCatalogService;

  public ProvisioningController(ProvisioningService provisioningService,
      ModuleCatalogService moduleCatalogService) {
    this.provisioningService = provisioningService;
    this.moduleCatalogService = moduleCatalogService;
  }

  @PostMapping("/tenants/{tenantId}/provision")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> provisionTenant(
      @PathVariable Long tenantId,
      @Valid @RequestBody ProvisionRequest request) {

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




