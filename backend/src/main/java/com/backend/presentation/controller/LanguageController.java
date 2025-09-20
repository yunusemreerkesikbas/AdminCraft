package com.backend.presentation.controller;

import com.backend.application.service.LanguageService;
import com.backend.presentation.dto.request.UpdateTenantLanguagesRequest;
import com.backend.presentation.dto.response.LanguageCatalogItem;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/languages")
@Validated
public class LanguageController {

  private final LanguageService languageService;

  public LanguageController(LanguageService languageService) {
    this.languageService = languageService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<LanguageCatalogItem>>> getPlatformLanguages() {
    try {
      List<LanguageCatalogItem> data = languageService.getPlatformLanguages();
      return ResponseEntity.ok(ApiResponse.success("language.catalog.success", data));
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "language.catalog.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/tenant")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> getTenantLanguages(
      @RequestHeader("X-Tenant-ID") Long tenantId) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"),
            HttpStatus.FORBIDDEN);
      }
      var data = languageService.getTenantLanguages(tenantId);
      return ResponseEntity.ok(ApiResponse.success("language.tenant.get.success", data));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(404, ex.getMessage()), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "language.tenant.get.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PatchMapping("/tenant")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> updateTenantLanguages(
      @RequestHeader("X-Tenant-ID") Long tenantId,
      @Valid @RequestBody UpdateTenantLanguagesRequest request) {
    try {
      Long userTenantId = SecurityUtil.getCurrentUserTenantId();
      if (userTenantId != null && !userTenantId.equals(tenantId)) {
        return new ResponseEntity<>(ApiResponse.error(403, "common.tenant.mismatch"),
            HttpStatus.FORBIDDEN);
      }
      var data = languageService.updateTenantLanguages(tenantId, request);
      return ResponseEntity.ok(ApiResponse.success("language.tenant.update.success", data));
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(ApiResponse.error(500, "language.tenant.update.error"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
