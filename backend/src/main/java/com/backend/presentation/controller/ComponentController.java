package com.backend.presentation.controller;

import com.backend.application.service.ComponentService;
import com.backend.domain.exception.ComponentConflictException;
import com.backend.domain.exception.ComponentNotFoundException;
import com.backend.presentation.dto.request.CreateComponentRequest;
import com.backend.presentation.dto.request.UpdateComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import com.backend.shared.common.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

  private final ComponentService service;

  // Constructor injection following Clean Architecture principles
  public ComponentController(ComponentService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ComponentResponse>>> list(
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      // Validate tenant access
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      List<ComponentResponse> data = service.list(tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.list.success", data));
    } catch (Exception ex) {
      // Let GlobalExceptionHandler handle specific exceptions
      throw new RuntimeException("Failed to list components", ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> get(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      // Validate tenant access
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      ComponentResponse data = service.get(id, tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.get.success", data));
    } catch (ComponentNotFoundException ex) {
      // Let GlobalExceptionHandler handle this specific exception
      throw ex;
    } catch (Exception ex) {
      // Let GlobalExceptionHandler handle generic exceptions
      throw new RuntimeException("Failed to get component", ex);
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ComponentResponse>> create(
      @Valid @RequestBody CreateComponentRequest request,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @RequestHeader("X-User-ID") Long userId) {
    try {
      // Validate tenant access
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      // Validate request tenant ID matches header
      if (!request.tenantId().equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      ComponentResponse data = service.create(request, userId);
      return new ResponseEntity<>(ApiResponse.success("ui.component.create.success", data), HttpStatus.CREATED);
    } catch (ComponentConflictException ex) {
      // Let GlobalExceptionHandler handle this specific exception
      throw ex;
    } catch (Exception ex) {
      // Let GlobalExceptionHandler handle generic exceptions
      throw new RuntimeException("Failed to create component", ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> update(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody UpdateComponentRequest request,
      @RequestHeader("X-User-ID") Long userId) {
    try {
      // Validate tenant access
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      ComponentResponse data = service.update(id, tenantId, request, userId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.update.success", data));
    } catch (ComponentNotFoundException ex) {
      // Let GlobalExceptionHandler handle this specific exception
      throw ex;
    } catch (Exception ex) {
      // Let GlobalExceptionHandler handle generic exceptions
      throw new RuntimeException("Failed to update component", ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      // Validate tenant access
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      
      service.delete(id, tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.delete.success", null));
    } catch (ComponentNotFoundException ex) {
      // Let GlobalExceptionHandler handle this specific exception
      throw ex;
    } catch (Exception ex) {
      // Let GlobalExceptionHandler handle generic exceptions
      throw new RuntimeException("Failed to delete component", ex);
    }
  }
}