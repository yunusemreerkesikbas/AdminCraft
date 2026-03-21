package com.backend.presentation.controller;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.dto.provisioning.SyncMigrationsRequest;
import com.backend.application.dto.request.ProvisionLanguagesRequest;
import com.backend.application.dto.request.TenantLanguagesUpdateRequest;
import com.backend.application.service.ProvisioningService;
import com.backend.application.service.TenantLanguageService;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import com.backend.shared.common.ApiResponse;
import com.google.common.util.concurrent.RateLimiter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantLanguageController {

  private final TenantLanguageService tenantLanguageService;
  private final ProvisioningService provisioningService;
  private final MessageSource messageSource;

  private final ConcurrentHashMap<Long, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
  private static final double PERMITS_PER_MINUTE = 20.0;
  private static final double PERMITS_PER_SECOND = PERMITS_PER_MINUTE / 60.0;

  private RateLimiter getRateLimiter(Long tenantId) {
    return rateLimiters.computeIfAbsent(tenantId, id -> RateLimiter.create(PERMITS_PER_SECOND));
  }

  private <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, String lang, String messageKey, T data) {
    return ResponseEntity.status(status).body(ApiResponse.success(
        messageSource.getMessage(messageKey, null, Locale.forLanguageTag(lang)), data));
  }

  private <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String lang, String fallbackKey) {
    return ResponseEntity.status(status).body(ApiResponse.error(
        messageSource.getMessage(fallbackKey, null, Locale.forLanguageTag(lang))));
  }

  @GetMapping("/{tenantId}/languages")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> getLanguages(
      @PathVariable Long tenantId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      TenantLanguagesResponse response = tenantLanguageService.getLanguages(tenantId);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting tenant languages for tenantId={}", tenantId, ex);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, lang, "tenant.languages.get.error");
    }
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PutMapping("/{tenantId}/languages")
  public ResponseEntity<ApiResponse<TenantLanguagesResponse>> updateLanguages(
      @PathVariable Long tenantId,
      @Valid @RequestBody TenantLanguagesUpdateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      if (!request.isValid()) {
        String validationErrorKey = request.getValidationErrorMessageKey();
        String validationError = messageSource.getMessage(validationErrorKey, null, Locale.forLanguageTag(lang));
        String message = messageSource.getMessage("tenant.languages.validation.error",
            new Object[] { validationError }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
      }

      TenantLanguagesResponse response = tenantLanguageService.updateLanguages(tenantId, request);
      return success(HttpStatus.OK, lang, "tenant.languages.update.success", response);
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error updating tenant languages for tenantId={}: {}", tenantId, ex.getMessage());
      return error(HttpStatus.BAD_REQUEST, lang, "tenant.languages.validation.error");
    } catch (Exception ex) {
      log.error("Error updating tenant languages for tenantId={}", tenantId, ex);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, lang, "tenant.languages.update.error");
    }
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PostMapping("/{tenantId}/languages/provision")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> provisionLanguages(
      @PathVariable Long tenantId,
      @Valid @RequestBody ProvisionLanguagesRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    RateLimiter rateLimiter = getRateLimiter(tenantId);
    if (!rateLimiter.tryAcquire()) {
      log.warn("Language provisioning rate limit exceeded for tenant {}", tenantId);
      String rateLimitMessage = messageSource.getMessage("provisioning.rate.limit.exceeded", null,
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(rateLimitMessage));
    }

    try {
      log.info("Provisioning languages for tenantId={}: {}", tenantId, request.getLanguages());
      SyncMigrationsRequest syncRequest = new SyncMigrationsRequest();
      ProvisioningJobResponse response = provisioningService.syncTenantMigrations(tenantId, syncRequest);

      String successMessage = messageSource.getMessage("tenant.languages.provision.success",
          null, Locale.forLanguageTag(lang));
      if (successMessage.equals("tenant.languages.provision.success")) {
        successMessage = "Language provisioning started successfully";
      }
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (IllegalArgumentException | IllegalStateException ex) {
      log.warn("Language provisioning validation error for tenantId={}: {}", tenantId, ex.getMessage());
      return error(HttpStatus.BAD_REQUEST, lang, "tenant.languages.provision.error");
    } catch (Exception ex) {
      log.error("Error provisioning languages for tenantId={}", tenantId, ex);
      return error(HttpStatus.INTERNAL_SERVER_ERROR, lang, "tenant.languages.provision.error");
    }
  }

}
