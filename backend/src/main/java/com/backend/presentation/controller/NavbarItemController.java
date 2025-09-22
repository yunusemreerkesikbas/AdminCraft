package com.backend.presentation.controller;

import com.backend.application.service.ComponentItemService;
import com.backend.domain.enums.ComponentType;
import com.backend.presentation.dto.request.NavbarItemRequest;
import com.backend.presentation.dto.request.NavbarItemsReorderRequest;
import com.backend.presentation.dto.response.NavbarItemResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/components/navbar")
public class NavbarItemController {

  private final ComponentItemService service;

  public NavbarItemController(ComponentItemService service) {
    this.service = service;
  }

  @GetMapping("/{id}/items/tree")
  public ResponseEntity<ApiResponse<List<NavbarItemResponse>>> listTree(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("id") Long componentId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      var data = service.listTree(tenantId, componentId);
      return ResponseEntity.ok(ApiResponse.success("ui.navbar.items.tree.success", data));
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "ui.navbar.items.tree.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/{id}/items")
  public ResponseEntity<ApiResponse<NavbarItemResponse>> create(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("id") Long componentId,
      @Valid @RequestBody NavbarItemRequest request) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      var data = service.create(tenantId, componentId, request);
      return new ResponseEntity<>(ApiResponse.success("ui.navbar.item.create.success", data), HttpStatus.CREATED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "ui.navbar.item.create.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{id}/items/{itemId}")
  public ResponseEntity<ApiResponse<NavbarItemResponse>> update(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("id") Long componentId,
      @PathVariable Long itemId,
      @Valid @RequestBody NavbarItemRequest request) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      var data = service.update(tenantId, componentId, itemId, request);
      return ResponseEntity.ok(ApiResponse.success("ui.navbar.item.update.success", data));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "ui.navbar.item.update.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}/items/{itemId}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("id") Long componentId,
      @PathVariable Long itemId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      service.delete(tenantId, componentId, itemId);
      return ResponseEntity.ok(ApiResponse.success("ui.navbar.item.delete.success", null));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "ui.navbar.item.delete.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PatchMapping("/{id}/items/reorder")
  public ResponseEntity<ApiResponse<Void>> reorder(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("id") Long componentId,
      @Valid @RequestBody NavbarItemsReorderRequest request) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      service.reorder(tenantId, componentId, request);
      return ResponseEntity.ok(ApiResponse.success("ui.navbar.items.reorder.success", null));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "ui.navbar.items.reorder.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

