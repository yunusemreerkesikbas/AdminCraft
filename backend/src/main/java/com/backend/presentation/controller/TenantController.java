package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.CreateTenantRequest;
import com.backend.application.dto.request.UpdateTenantRequest;
import com.backend.application.dto.response.AdminUserResponse;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.application.service.TenantService;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.application.usecase.GenerateTenantAdminUserUseCase;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.response.ProvisioningJobResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        private final SecurityHelper securityHelper;
        private final com.backend.infrastructure.tenant.TenantContext tenantContext;

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PostMapping
        public ResponseEntity<ApiResponse<TenantDetailResponse>> createTenant(
                        @Valid @RequestBody CreateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                TenantDetailResponse response = createTenantUseCase.execute(request, displayLanguage);
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
        public ResponseEntity<ApiResponse<PageableResponse<TenantListResponse>>> listTenants(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) TenantStatus status,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        String effectiveSort = SortParseUtil.getEffectiveSortCode(
                                        sort,
                                        SortableFieldsConfig.TENANT_DEFAULT_SORT);
                        Sort sortObj = SortParseUtil.parse(
                                        effectiveSort,
                                        SortableFieldsConfig.TENANT_ALLOWED_FIELDS,
                                        SortableFieldsConfig.TENANT_DEFAULT_SORT);

                        Pageable pageable = PageRequest.of(page, size, sortObj);
                        Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                        Page<TenantListResponse> tenantPage = tenantService.searchTenants(
                                        search,
                                        status,
                                        pageable,
                                        displayLanguage);

                        SortConfig sortConfig = SortConfig.of(
                                        effectiveSort,
                                        SortableFieldsConfig.TENANT_SORT_OPTIONS);
                        PageableResponse<TenantListResponse> response = PageableResponse.from(
                                        tenantPage,
                                        sortConfig);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (IllegalArgumentException ex) {
                        String message = messageSource.getMessage(
                                        "tenant.sort.invalid",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> updateTenant(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                TenantDetailResponse response = tenantService.updateTenantWithDetail(id, request, displayLanguage);
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

        @GetMapping("/{tenantId}/provisioning-jobs")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<ProvisioningJobResponse>>> getTenantProvisioningJobs(
                        @PathVariable @NotNull @Min(1) Long tenantId) {
                List<ProvisioningJobResponse> response = tenantService.getTenantProvisioningJobs(tenantId).stream()
                                .map(ProvisioningJobResponse::from)
                                .toList();
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping("/current/modules")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
        public ResponseEntity<ApiResponse<List<TenantModuleResponse>>> getCurrentUserModules(
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Long userTenantId = securityHelper.getCurrentUserTenantId();
                        String headerTenantId = tenantContext.getTenantId();

                        if (headerTenantId != null && !userTenantId.equals(Long.parseLong(headerTenantId))) {
                                String message = messageSource.getMessage("tenant.id.mismatch",
                                                null, Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(ApiResponse.error(message));
                        }

                        Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                        List<TenantModuleResponse> modules = tenantService.getTenantModules(userTenantId,
                                        displayLanguage);
                        log.debug("Successfully fetched {} modules for tenant: {}", modules.size(), userTenantId);
                        return ResponseEntity.ok(ApiResponse.success(modules));
                } catch (Exception ex) {
                        log.error("Error fetching modules for current user: {}", ex.getMessage(), ex);
                        String message = messageSource.getMessage("tenant.modules.get.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/current/detail")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> getCurrentTenantDetail(
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Long userTenantId = securityHelper.getCurrentUserTenantId();
                        Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                        TenantDetailResponse response = tenantService.getTenantDetailById(userTenantId,
                                        displayLanguage);
                        log.debug("Successfully fetched tenant detail for tenant: {}", userTenantId);
                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (Exception ex) {
                        log.error("Error fetching tenant detail for current user: {}", ex.getMessage(), ex);
                        String message = messageSource.getMessage("tenant.detail.get.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }
}
