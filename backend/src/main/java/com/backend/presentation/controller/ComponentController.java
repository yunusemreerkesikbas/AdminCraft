package com.backend.presentation.controller;

import com.backend.application.service.ComponentService;
import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import com.backend.shared.common.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/components")
public class ComponentController {

  private final ComponentService service;

  // Constructor injection following Clean Architecture principles
  public ComponentController(ComponentService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ComponentResponse>>> list(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @RequestParam(value = "type", required = false) String type) {
    // Validate tenant access
    Long userTenantId = SecurityUtil.getCurrentUserTenantId();
    if (userTenantId != null && !userTenantId.equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }

    List<ComponentResponse> data = (type == null)
        ? service.list(tenantId)
        : service.list(tenantId, new ComponentListFilter(tenantId,
            com.backend.domain.enums.ComponentType.fromCode(type), null));
    return ResponseEntity.ok(ApiResponse.success("ui.component.list.success", data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> get(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    // Validate tenant access
    Long userTenantId = SecurityUtil.getCurrentUserTenantId();
    if (userTenantId != null && !userTenantId.equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }

    ComponentResponse data = service.get(id, tenantId);
    return ResponseEntity.ok(ApiResponse.success("ui.component.get.success", data));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ComponentResponse>> create(
      @Valid @RequestBody ComponentRequest request,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    // Validate tenant access
    Long userTenantId = SecurityUtil.getCurrentUserTenantId();
    if (userTenantId != null && !userTenantId.equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }

    // Validate request tenant ID matches header
    if (!request.tenantId().equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }
    ComponentResponse data = service.create(tenantId, request);

    return new ResponseEntity<>(ApiResponse.success("ui.component.create.success", data), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> update(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody ComponentRequest request) {
    // Validate tenant access
    Long userTenantId = SecurityUtil.getCurrentUserTenantId();
    if (userTenantId != null && !userTenantId.equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }
    ComponentResponse data = service.update(id, tenantId, request);

    return ResponseEntity.ok(ApiResponse.success("ui.component.update.success", data));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    // Validate tenant access
    Long userTenantId = SecurityUtil.getCurrentUserTenantId();
    if (userTenantId != null && !userTenantId.equals(tenantId)) {
      return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
    }

    service.delete(id, tenantId);
    return ResponseEntity.ok(ApiResponse.success("ui.component.delete.success", null));
  }
}