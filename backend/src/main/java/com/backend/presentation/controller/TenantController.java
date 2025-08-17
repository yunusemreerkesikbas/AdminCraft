package com.backend.presentation.controller;

import com.backend.application.service.TenantService;
import com.backend.application.usecase.ActivateTenantUseCase;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TenantController {

    private final TenantService tenantService;
    private final CreateTenantUseCase createTenantUseCase;
    private final ActivateTenantUseCase activateTenantUseCase;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = createTenantUseCase.execute(request, displayLanguage);
            String message = messageSource.getMessage("tenant.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error creating tenant: {}", ex.getMessage());
            String message = messageSource.getMessage("tenant.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = tenantService.getTenantById(id, displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting tenant by id {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantBySubdomain(
            @PathVariable @Valid @NotBlank String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize subdomain input
            String sanitizedSubdomain = sanitizeInput(subdomain);
            if (sanitizedSubdomain == null || sanitizedSubdomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid subdomain");
            }
            
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = tenantService.getTenantBySubdomain(sanitizedSubdomain, displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting tenant by subdomain {}: {}", subdomain, ex.getMessage());
            String message = messageSource.getMessage("tenant.subdomain.not.found", new Object[]{subdomain}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants(
            @RequestParam(required = false) TenantStatus status,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            List<TenantResponse> response = status != null 
                ? tenantService.getTenantsByStatus(status, displayLanguage)
                : tenantService.getAllTenants(displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting all tenants: {}", ex.getMessage());
            String message = messageSource.getMessage("tenant.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @Valid @RequestBody UpdateTenantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = tenantService.updateTenant(id, request, displayLanguage);
            String message = messageSource.getMessage("tenant.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error updating tenant {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = activateTenantUseCase.execute(id, displayLanguage);
            String message = messageSource.getMessage("tenant.activated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error activating tenant {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.activate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<TenantResponse>> suspendTenant(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = tenantService.suspendTenant(id, displayLanguage);
            String message = messageSource.getMessage("tenant.suspended.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error suspending tenant {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.suspend.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<TenantResponse>> setMaintenanceMode(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            TenantResponse response = tenantService.setMaintenanceMode(id, displayLanguage);
            String message = messageSource.getMessage("tenant.maintenance.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error setting maintenance mode for tenant {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.maintenance.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            tenantService.deleteTenant(id);
            String message = messageSource.getMessage("tenant.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting tenant {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("tenant.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<Boolean>> checkSubdomainAvailability(
            @PathVariable @Valid @NotBlank String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize subdomain input
            String sanitizedSubdomain = sanitizeInput(subdomain);
            if (sanitizedSubdomain == null || sanitizedSubdomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid subdomain");
            }
            
            boolean available = tenantService.isSubdomainAvailable(sanitizedSubdomain);
            String messageKey = available ? "tenant.subdomain.available" : "tenant.subdomain.taken";
            String message = messageSource.getMessage(messageKey, new Object[]{subdomain}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            log.error("Error checking subdomain availability for {}: {}", subdomain, ex.getMessage());
            String message = messageSource.getMessage("tenant.subdomain.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getTenantCountByStatus(
            @RequestParam @Valid @NotNull TenantStatus status,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            long count = tenantService.getTenantCountByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception ex) {
            log.error("Error getting tenant count by status {}: {}", status, ex.getMessage());
            String message = messageSource.getMessage("tenant.stats.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Sanitizes input to prevent injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Basic input sanitization for subdomains - only allow alphanumeric and hyphens
        return input.trim()
                .replaceAll("[^a-zA-Z0-9-]", "")
                .toLowerCase();
    }
}