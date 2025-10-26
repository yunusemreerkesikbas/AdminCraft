package com.backend.presentation.controller;

import com.backend.application.service.ProvisioningService;
import com.backend.application.service.TenantLanguageService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.LanguageProvisioningRequest;
import com.backend.presentation.dto.request.TenantLanguagesUpdateRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
public class TenantLanguageController {

  private final TenantLanguageService tenantLanguageService;
  private final ProvisioningService provisioningService;
  private final MessageSource messageSource;

  @GetMapping("/{tenantId}/languages")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> getLanguages(
      @PathVariable @NotNull @Min(1) Long tenantId,
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
      @PathVariable @NotNull @Min(1) Long tenantId,
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

  // TODO: Language provisioning removed - Sprint 12 refactored to database-per-tenant architecture
  // @PostMapping("/{tenantId}/languages/provision")
  // public ResponseEntity<ApiResponse<ProvisioningJobResponse>> provisionLanguages(
  //     @PathVariable @NotNull @Min(1) Long tenantId,
  //     @Valid @RequestBody LanguageProvisioningRequest request,
  //     @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
  //   try {
  //     Set<Language> languagesToProvision = request.languages();
  //
  //     if (languagesToProvision.isEmpty()) {
  //       String message = messageSource.getMessage("tenant.languages.provision.none",
  //           null, Locale.forLanguageTag(lang));
  //       return ResponseEntity.ok(ApiResponse.success(message, null));
  //     }
  //
  //     ProvisioningJobResponse jobResponse = provisioningService.createLanguageProvisioningJob(
  //         tenantId, languagesToProvision);
  //
  //     String successMessage = messageSource.getMessage("tenant.languages.provision.started",
  //         new Object[] { jobResponse.uuid() }, Locale.forLanguageTag(lang));
  //     return ResponseEntity.ok(ApiResponse.success(successMessage, jobResponse));
  //   } catch (Exception ex) {
  //     log.error("Error provisioning languages for tenantId={}: {}", tenantId, ex.getMessage());
  //     String message = messageSource.getMessage("tenant.languages.provision.error",
  //         new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
  //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //         .body(ApiResponse.error(message));
  //   }
  // }
}
