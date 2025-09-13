package com.backend.presentation.controller;

import com.backend.application.service.ComponentService;
import com.backend.presentation.dto.request.CreateComponentRequest;
import com.backend.presentation.dto.request.UpdateComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import com.backend.shared.common.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

  @Autowired
  private ComponentService service;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ComponentResponse>>> list(
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      List<ComponentResponse> data = service.list(tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.list.success", data));
    } catch (Exception ex) {
      return new ResponseEntity<>(ApiResponse.error(500, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> get(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      ComponentResponse data = service.get(id, tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.get.success", data));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(404, ex.getMessage()), HttpStatus.NOT_FOUND);
    } catch (Exception ex) {
      return new ResponseEntity<>(ApiResponse.error(500, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ComponentResponse>> create(
      @Valid @RequestBody CreateComponentRequest request,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @RequestHeader("X-User-ID") Long userId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      if (!request.tenantId().equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      ComponentResponse data = service.create(request, userId);
      return new ResponseEntity<>(ApiResponse.success("ui.component.create.success", data), HttpStatus.CREATED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(409, ex.getMessage()), HttpStatus.CONFLICT);
    } catch (Exception ex) {
      return new ResponseEntity<>(ApiResponse.error(500, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> update(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody UpdateComponentRequest request,
      @RequestHeader("X-User-ID") Long userId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      ComponentResponse data = service.update(id, tenantId, request, userId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.update.success", data));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(404, ex.getMessage()), HttpStatus.NOT_FOUND);
    } catch (Exception ex) {
      return new ResponseEntity<>(ApiResponse.error(500, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      service.delete(id, tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.delete.success", null));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(404, ex.getMessage()), HttpStatus.NOT_FOUND);
    } catch (Exception ex) {
      return new ResponseEntity<>(ApiResponse.error(500, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
