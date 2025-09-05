package com.backend.presentation.controller;

import com.backend.application.dto.media.MediaUsageDto;
import com.backend.application.service.MediaAttachmentService;
import com.backend.domain.enums.MediaPurpose;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/media-attachments")
@RequiredArgsConstructor
@Slf4j
public class MediaAttachmentController {

  private final MediaAttachmentService service;
  private final SecurityHelper securityHelper;

  @PostMapping
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
  public ResponseEntity<ApiResponse<List<MediaUsageDto>>> attach(
      @RequestParam Long tenantId,
      @RequestParam String ownerType,
      @RequestParam Long ownerId,
      @RequestParam List<Long> mediaIds,
      @RequestParam(defaultValue = "THUMBNAIL") MediaPurpose purpose) {
    try {
      securityHelper.validateTenantAccess(tenantId);
      return ResponseEntity.ok(ApiResponse.success(
          service.attach(tenantId, ownerType, ownerId, mediaIds, purpose)));
    } catch (Exception e) {
      log.error("Attach error", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PatchMapping("/cover")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
  public ResponseEntity<ApiResponse<MediaUsageDto>> setCover(
      @RequestParam Long tenantId,
      @RequestParam String ownerType,
      @RequestParam Long ownerId,
      @RequestParam Long usageId) {
    try {
      securityHelper.validateTenantAccess(tenantId);
      return ResponseEntity.ok(ApiResponse.success(
          service.setCover(tenantId, ownerType, ownerId, usageId)));
    } catch (Exception e) {
      log.error("Set cover error", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PatchMapping("/reorder")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
  public ResponseEntity<ApiResponse<List<MediaUsageDto>>> reorder(
      @RequestParam Long tenantId,
      @RequestParam String ownerType,
      @RequestParam Long ownerId,
      @RequestParam List<Long> orderedUsageIds) {
    try {
      securityHelper.validateTenantAccess(tenantId);
      return ResponseEntity.ok(ApiResponse.success(
          service.reorder(tenantId, ownerType, ownerId, orderedUsageIds)));
    } catch (Exception e) {
      log.error("Reorder error", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @DeleteMapping
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
  public ResponseEntity<ApiResponse<Void>> detach(
      @RequestParam Long tenantId,
      @RequestParam String ownerType,
      @RequestParam Long ownerId,
      @RequestParam Long usageId) {
    try {
      securityHelper.validateTenantAccess(tenantId);
      service.detach(tenantId, ownerType, ownerId, usageId);
      return ResponseEntity.ok(ApiResponse.success(null));
    } catch (Exception e) {
      log.error("Detach error", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}
