package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.TenantLanguageService;
import com.backend.presentation.dto.request.TenantLanguagesUpdateRequest;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class TenantLanguageController {

  private final TenantLanguageService tenantLanguageService;
  private final MessageSource messageSource;

  @GetMapping("/{tenantId}/languages")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> getLanguages(
      @PathVariable Long tenantId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      TenantLanguagesResponse response = tenantLanguageService.getLanguages(tenantId);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting tenant languages for tenantId={}: {}", tenantId, ex.getMessage());
      String message = messageSource.getMessage("tenant.languages.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  @PutMapping("/{tenantId}/languages")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> updateLanguages(
      @PathVariable Long tenantId,
      @Valid @RequestBody TenantLanguagesUpdateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      if (!request.isValid()) {
        String validationError = request.getValidationErrorMessage();
        String message = messageSource.getMessage("tenant.languages.validation.error",
            new Object[] { validationError }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
      }

      TenantLanguagesResponse response = tenantLanguageService.updateLanguages(tenantId, request);

      String successMessage = messageSource.getMessage("tenant.languages.update.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error updating tenant languages for tenantId={}: {}", tenantId, ex.getMessage());
      String message = messageSource.getMessage("tenant.languages.validation.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error updating tenant languages for tenantId={}: {}", tenantId, ex.getMessage());
      String message = messageSource.getMessage("tenant.languages.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

}
