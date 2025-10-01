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

  @GetMapping
  public ResponseEntity<ApiResponse<List<ComponentResponse>>> list(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @RequestParam(value = "type", required = false) String type) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }
      if (type == null || type.trim().isEmpty()) {
        List<ComponentResponse> data = service.list(tenantId);
        return ResponseEntity.ok(ApiResponse.success("ui.component.list.success", data));
      } else {
        try {
          ComponentType componentType = ComponentType.fromCode(type.trim());
          ComponentListFilter filter = new ComponentListFilter(tenantId, componentType, null);
          List<ComponentResponse> data = service.list(tenantId, filter);
          return ResponseEntity.ok(ApiResponse.success("ui.component.list.filtered.success", data));
        } catch (IllegalArgumentException e) {
          String errorMessage = "Invalid component type: " + type + ". Supported types: " + getSupportedTypesMessage();
          return new ResponseEntity<>(
              ApiResponse.error(400, errorMessage),
              HttpStatus.BAD_REQUEST);
        }
      }
    } catch (Exception e) {
      return new ResponseEntity<>(
          ApiResponse.error(500, "ui.component.list.server.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
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

  @PostMapping
  public ResponseEntity<ApiResponse<ComponentResponse>> create(
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

      if (request.type() == null) {
        return new ResponseEntity<>(
            ApiResponse.error(400, "Component type is required"),
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

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ComponentResponse>> update(
      @PathVariable Long id,
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody ComponentRequest request) {

    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"), HttpStatus.FORBIDDEN);
      }

      ComponentResponse existing = service.get(id, tenantId);
      if (request.type() != null && !request.type().equals(existing.type())) {
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

  @GetMapping("/site-components")
  public ResponseEntity<ApiResponse<List<SiteComponentResponse>>> getSiteComponents(
      @RequestParam("type") String type,
      @RequestParam("lang") String lang,
      @RequestHeader("X-Tenant-ID") Long tenantId) {

    try {
      ComponentType componentType;
      try {
        componentType = ComponentType.fromCode(type);
      } catch (IllegalArgumentException e) {
        String errorMessage = "Invalid component type: " + type + ". Supported types: " + getSupportedTypesMessage();
        return new ResponseEntity<>(
            ApiResponse.error(400, errorMessage),
            HttpStatus.BAD_REQUEST);
      }

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

      List<SiteComponentResponse> data = service.getSiteComponents(tenantId, componentType, language);

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