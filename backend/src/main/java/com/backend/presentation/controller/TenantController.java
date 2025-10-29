package com.backend.presentation.controller;

import com.backend.application.dto.response.AdminUserResponse;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.application.service.TenantService;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.application.usecase.GenerateTenantAdminUserUseCase;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
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
        private final GenerateTenantAdminUserUseCase generateTenantAdminUserUseCase;

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PostMapping
        public ResponseEntity<ApiResponse<TenantDetailResponse>> createTenant(
                        @Valid @RequestBody CreateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                var cmd = new com.backend.application.command.CreateTenantCommand(
                                request.subdomain(), request.companyName(), request.defaultLanguage(),
                                request.supportedLanguages(), request.notes(), null, null);
                TenantDetailResponse response = createTenantUseCase.execute(cmd, displayLanguage);
                String message = messageSource.getMessage("tenant.created.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(message, response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> getTenantById(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                TenantDetailResponse response = tenantService.getTenantDetailById(id, displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<TenantListResponse>>> getAllTenants(
                        @RequestParam(required = false) TenantStatus status,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                List<TenantListResponse> response = status != null
                                ? tenantService.getTenantsByStatusAsList(status, displayLanguage)
                                : tenantService.getAllTenantsAsList(displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> updateTenant(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                var cmd = new com.backend.application.command.UpdateTenantCommand(
                                request.companyName(), request.defaultLanguage(), request.supportedLanguages(),
                                request.customDomain(), request.notes());
                TenantDetailResponse response = tenantService.updateTenantWithDetail(id, cmd, displayLanguage);
                String message = messageSource.getMessage("tenant.updated.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteTenant(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                tenantService.deleteTenant(id);
                String message = messageSource.getMessage("tenant.deleted.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, null));
        }

        @GetMapping("/{tenantId}/modules")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<TenantModuleResponse>>> getTenantModules(
                        @PathVariable @Valid @NotNull @Min(1) Long tenantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                List<TenantModuleResponse> modules = tenantService.getTenantModules(tenantId, displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(modules));
        }

        @PostMapping("/{id}/generate-admin")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<AdminUserResponse>> generateAdminUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        AdminUserResponse response = generateTenantAdminUserUseCase.execute(id);
                        String message = messageSource.getMessage("tenant.admin.generated.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, response));
                } catch (IllegalStateException ex) {
                        String code = ex.getMessage();
                        String msg = messageSource.getMessage(code != null ? code : "error.runtime", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(ApiResponse.<AdminUserResponse>error(HttpStatus.CONFLICT.value(), msg));
                }
        }
}