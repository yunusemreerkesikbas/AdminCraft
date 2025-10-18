package com.backend.presentation.controller;

import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.application.service.TenantService;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;
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

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TenantController {

        private final TenantService tenantService;
        private final CreateTenantUseCase createTenantUseCase;
        private final MessageSource messageSource;

        @PostMapping
        public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
                        @Valid @RequestBody CreateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                        TenantResponse response = createTenantUseCase.execute(request, displayLanguage);
                        String message = messageSource.getMessage("tenant.created.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, response));
                } catch (Exception ex) {
                        log.error("Error creating tenant: {}", ex.getMessage());
                        String message = messageSource.getMessage("tenant.create.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
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
                        String message = messageSource.getMessage("tenant.not.found", new Object[] { id },
                                        Locale.forLanguageTag(languageCode));
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
                        String message = messageSource.getMessage("tenant.list.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
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
                        String message = messageSource.getMessage("tenant.updated.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, response));
                } catch (Exception ex) {
                        log.error("Error updating tenant {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("tenant.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
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
                        String message = messageSource.getMessage("tenant.deleted.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (Exception ex) {
                        log.error("Error deleting tenant {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("tenant.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/{tenantId}/modules")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<TenantModuleResponse>>> getTenantModules(
                        @PathVariable @Valid @NotNull @Min(1) Long tenantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                        List<TenantModuleResponse> modules = tenantService.getTenantModules(tenantId, displayLanguage);
                        return ResponseEntity.ok(ApiResponse.success(modules));
                } catch (Exception ex) {
                        log.error("Error fetching modules for tenant {}: {}", tenantId, ex.getMessage());
                        String message = messageSource.getMessage("tenant.modules.error",
                                        new Object[] { tenantId, ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }
}