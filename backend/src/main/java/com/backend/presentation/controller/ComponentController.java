package com.backend.presentation.controller;

import com.backend.application.service.ComponentI18nService;
import com.backend.application.service.ComponentService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentCreateRequest;
import com.backend.presentation.dto.request.ComponentI18nRequest;
import com.backend.presentation.dto.response.ComponentDetailResponse;
import com.backend.presentation.dto.response.ComponentI18nResponse;
import com.backend.presentation.dto.response.ComponentListResponse;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
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
@RequestMapping("/components")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class ComponentController {

    private final ComponentService componentService;
    private final ComponentI18nService componentI18nService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<ComponentResponse>> create(
        @Valid @RequestBody ComponentCreateRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            ComponentResponse response = componentService.createComponent(request, userId);
            String successMessage = messageSource.getMessage("component.create.success",
                null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error creating component: {}", ex.getMessage());
            String msg = messageSource.getMessage("component.create.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(
        @PathVariable @NotNull @Min(1) Long id,
        @RequestParam(value = "include", required = false) String include,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            if (include != null && include.contains("translations")) {
                ComponentDetailResponse withI18n = componentService.getComponentWithI18n(id);
                return ResponseEntity.ok(ApiResponse.success(withI18n));
            }
            ComponentResponse response = componentService.getComponentById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting component {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.get.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponentListResponse>>> list(
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            List<ComponentListResponse> components = componentService.getAllComponentsWithTranslations();
            return ResponseEntity.ok(ApiResponse.success(components));
        } catch (Exception ex) {
            log.error("Error listing components: {}", ex.getMessage());
            String msg = messageSource.getMessage("component.list.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentResponse>> update(
        @PathVariable @NotNull @Min(1) Long id,
        @Valid @RequestBody ComponentCreateRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            ComponentResponse response = componentService.updateComponent(id, request, userId);
            String successMessage = messageSource.getMessage("component.update.success",
                null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error updating component {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.update.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable @NotNull @Min(1) Long id,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            componentService.deleteComponent(id);
            String successMessage = messageSource.getMessage("component.delete.success",
                null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, null));
        } catch (Exception ex) {
            log.error("Error deleting component {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.delete.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}/i18n/{language}")
    public ResponseEntity<ApiResponse<ComponentI18nResponse>> getComponentI18n(
        @PathVariable @NotNull @Min(1) Long id,
        @PathVariable Language language,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            ComponentI18nResponse response = componentI18nService.getComponentI18n(id, language);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting component i18n for component {} language {}: {}", id, language, ex.getMessage());
            String msg = messageSource.getMessage("component.i18n.get.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{id}/i18n/{language}")
    public ResponseEntity<ApiResponse<ComponentI18nResponse>> upsertComponentI18n(
        @PathVariable @NotNull @Min(1) Long id,
        @PathVariable Language language,
        @Valid @RequestBody ComponentI18nRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            ComponentI18nResponse response = componentI18nService.upsertComponentI18n(id, language, request);
            String successMessage = messageSource.getMessage("component.i18n.upsert.success",
                null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error upserting component i18n for component {} language {}: {}", id, language, ex.getMessage());
            String msg = messageSource.getMessage("component.i18n.upsert.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
        }
    }

    @PostMapping("/{id}/publish/{language}")
    public ResponseEntity<ApiResponse<ComponentI18nResponse>> publishComponentI18n(
        @PathVariable @NotNull @Min(1) Long id,
        @PathVariable Language language,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            ComponentI18nResponse response = componentI18nService.publishComponentI18n(id, language);
            String successMessage = messageSource.getMessage("component.i18n.publish.success",
                null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error publishing component i18n for component {} language {}: {}", id, language, ex.getMessage());
            String msg = messageSource.getMessage("component.i18n.publish.error",
                new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
        }
    }
}
