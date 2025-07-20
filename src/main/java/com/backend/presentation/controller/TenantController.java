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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CreateTenantUseCase createTenantUseCase;

    @Autowired
    private ActivateTenantUseCase activateTenantUseCase;

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = createTenantUseCase.execute(request, displayLanguage);
            String message = messageSource.getMessage("tenant.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = tenantService.getTenantById(id, displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantBySubdomain(
            @PathVariable String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = tenantService.getTenantBySubdomain(subdomain, displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
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
            Language displayLanguage = Language.fromCode(languageCode);
            List<TenantResponse> response = status != null 
                ? tenantService.getTenantsByStatus(status, displayLanguage)
                : tenantService.getAllTenants(displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = tenantService.updateTenant(id, request, displayLanguage);
            String message = messageSource.getMessage("tenant.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = activateTenantUseCase.execute(id, displayLanguage);
            String message = messageSource.getMessage("tenant.activated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.activate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<TenantResponse>> suspendTenant(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = tenantService.suspendTenant(id, displayLanguage);
            String message = messageSource.getMessage("tenant.suspended.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.suspend.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<TenantResponse>> setMaintenanceMode(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCode(languageCode);
            TenantResponse response = tenantService.setMaintenanceMode(id, displayLanguage);
            String message = messageSource.getMessage("tenant.maintenance.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.maintenance.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            tenantService.deleteTenant(id);
            String message = messageSource.getMessage("tenant.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<Boolean>> checkSubdomainAvailability(
            @PathVariable String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            boolean available = tenantService.isSubdomainAvailable(subdomain);
            String messageKey = available ? "tenant.subdomain.available" : "tenant.subdomain.taken";
            String message = messageSource.getMessage(messageKey, new Object[]{subdomain}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.subdomain.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getTenantCountByStatus(
            @RequestParam TenantStatus status,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            long count = tenantService.getTenantCountByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception ex) {
            String message = messageSource.getMessage("tenant.stats.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}