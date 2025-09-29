package com.backend.presentation.controller;

import com.backend.application.service.ComponentService;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.SiteComponentResponse;
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

  public ComponentController(ComponentService service) {
    this.service = service;
  }

  @GetMapping("/{type}")
  public ResponseEntity<ApiResponse<List<ComponentResponse>>> listByType(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @PathVariable("type") ComponentType type,
      @RequestParam(value = "status", required = false) String status) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      ComponentListFilter filter;
      if (status != null && !status.isBlank()) {
        try {
          var st = com.backend.domain.enums.ComponentStatus.valueOf(status.toUpperCase());
          filter = new ComponentListFilter(tenantId, type, st);
        } catch (IllegalArgumentException ex) {
          return new ResponseEntity<>(ApiResponse.error(400, "ui.component.status.invalid"), HttpStatus.BAD_REQUEST);
        }
      } else {
        filter = new ComponentListFilter(tenantId, type, null);
      }
      List<ComponentResponse> data = service.list(tenantId, filter);
      return ResponseEntity.ok(ApiResponse.success("ui.component.list.filtered.success", data));
    } catch (Exception e) {
      return new ResponseEntity<>(
          ApiResponse.error(500, "ui.component.list.server.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{type}/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> get(
      @PathVariable("type") ComponentType type,
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {

    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      // Validate that the requested component matches the URL type
      ComponentResponse existing = service.get(id, tenantId);
      if (!existing.type().equals(type)) {
        return new ResponseEntity<>(
            ApiResponse.error(400, "ui.component.type.mismatch"),
            HttpStatus.BAD_REQUEST);
      }

      ComponentResponse data;
      if (ComponentType.NAVBAR.equals(type)) {
        data = service.getNavbarDetail(id, tenantId);
      } else {
        data = existing;
      }
      return ResponseEntity.ok(ApiResponse.success("ui.component.get.success", data));
    } catch (Exception e) {
      String errorMessage = "ui.component.get.error";
      HttpStatus status = HttpStatus.NOT_FOUND;

      if (e.getMessage() != null && e.getMessage().contains("not.found")) {
        errorMessage = "ui.component.not.found";
      } else if (!(e instanceof RuntimeException)) {
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        errorMessage = "ui.component.get.server.error";
      }

      return new ResponseEntity<>(ApiResponse.error(status.value(), errorMessage), status);
    }
  }

  @PostMapping("/{type}")
  public ResponseEntity<ApiResponse<ComponentResponse>> create(
      @PathVariable("type") ComponentType urlType,
      @Valid @RequestBody ComponentRequest request,
      @RequestHeader("X-Tenant-ID") Long tenantId) {

    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      if (!request.tenantId().equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      if (request.type() == null || !request.type().equals(urlType)) {
        return new ResponseEntity<>(
            ApiResponse.error(400, "ui.component.type.mismatch"),
            HttpStatus.BAD_REQUEST);
      }

      ComponentResponse data = service.create(tenantId, request);
      return new ResponseEntity<>(ApiResponse.success("ui.component.create.success", data), HttpStatus.CREATED);

    } catch (IllegalArgumentException e) {
      if (e.getMessage() != null && e.getMessage().contains("ComponentType")) {
        String errorMessage = "Invalid component type. Supported types: " + getSupportedTypesMessage();
        return new ResponseEntity<>(
            ApiResponse.error(400, errorMessage),
            HttpStatus.BAD_REQUEST);
      } else if (e.getMessage() != null && e.getMessage().contains("language")) {
        return new ResponseEntity<>(
            ApiResponse.error(400, "Invalid language code: " + e.getMessage()),
            HttpStatus.BAD_REQUEST);
      } else {
        return new ResponseEntity<>(
            ApiResponse.error(400, "Component validation error: " + e.getMessage()),
            HttpStatus.BAD_REQUEST);
      }
    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("conflict")) {
        return new ResponseEntity<>(
            ApiResponse.error(409, "Component with this type and key already exists for tenant"),
            HttpStatus.CONFLICT);
      } else {
        return new ResponseEntity<>(
            ApiResponse.error(500, "ui.component.create.server.error"),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  @PutMapping("/{type}/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> update(
      @PathVariable("type") ComponentType urlType,
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody ComponentRequest request) {

    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      ComponentResponse existing = service.get(id, tenantId);
      if ((request.type() != null && !request.type().equals(existing.type())) ||
          (urlType != null && !urlType.equals(existing.type()))) {
        String errorMessage = "Component type cannot be changed. Current type: " + existing.type().getCode();
        return new ResponseEntity<>(
            ApiResponse.error(400, errorMessage),
            HttpStatus.BAD_REQUEST);
      }

      ComponentResponse data = service.update(id, tenantId, request);
      return ResponseEntity.ok(ApiResponse.success("ui.component.update.success", data));

    } catch (IllegalArgumentException e) {
      if (e.getMessage() != null && e.getMessage().contains("language")) {
        return new ResponseEntity<>(
            ApiResponse.error(400, "Invalid language code: " + e.getMessage()),
            HttpStatus.BAD_REQUEST);
      } else {
        return new ResponseEntity<>(
            ApiResponse.error(400, "Component validation error: " + e.getMessage()),
            HttpStatus.BAD_REQUEST);
      }
    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("not.found")) {
        return new ResponseEntity<>(
            ApiResponse.error(404, "ui.component.not.found"),
            HttpStatus.NOT_FOUND);
      } else {
        return new ResponseEntity<>(
            ApiResponse.error(500, "ui.component.update.server.error"),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  @DeleteMapping("/{type}/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable("type") ComponentType type,
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId) {

    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      service.delete(id, tenantId);
      return ResponseEntity.ok(ApiResponse.success("ui.component.delete.success", null));

    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("not.found")) {
        return new ResponseEntity<>(
            ApiResponse.error(404, "ui.component.not.found"),
            HttpStatus.NOT_FOUND);
      } else {
        return new ResponseEntity<>(
            ApiResponse.error(500, "ui.component.delete.server.error"),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  @GetMapping("/{type}/site")
  public ResponseEntity<ApiResponse<List<SiteComponentResponse>>> getSiteComponents(
      @PathVariable("type") ComponentType type,
      @RequestParam("lang") String lang,
      @RequestHeader("X-Tenant-ID") Long tenantId) {

    try {
      Language language;
      try {
        language = Language.fromCode(lang)
            .orElseThrow(() -> new IllegalArgumentException("Invalid language code: " + lang));
      } catch (IllegalArgumentException e) {
        String errorMessage = "Invalid language code: " + lang + ". Supported languages: tr, en";
        return new ResponseEntity<>(
            ApiResponse.error(400, errorMessage),
            HttpStatus.BAD_REQUEST);
      }

      List<SiteComponentResponse> data = service.getSiteComponents(tenantId, type, language);

      if (data.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return ResponseEntity.ok()
          .header("Cache-Control", "public, max-age=300") // 5 minutes cache
          .body(ApiResponse.success("ui.component.site.list.success", data));

    } catch (Exception e) {
      String errorMessage = "Server error while fetching site components for type: " + type;
      return new ResponseEntity<>(
          ApiResponse.error(500, errorMessage),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String getSupportedTypesMessage() {
    return String.join(", ",
        ComponentType.NAVBAR.getCode(),
        ComponentType.LOGO.getCode(),
        ComponentType.CTA.getCode(),
        ComponentType.BRANDS.getCode(),
        ComponentType.FAQ.getCode(),
        ComponentType.BREADCRUMB.getCode());
  }
}