package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.command.ComponentI18nCommands.PublishComponentI18nCommand;
import com.backend.application.command.ComponentI18nCommands.UpsertComponentI18nCommand;
import com.backend.application.dto.request.ComponentCreateRequest;
import com.backend.application.dto.request.CreateComponentCompositeRequest;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.dto.response.BulkDeleteResultResponse;
import com.backend.application.dto.response.ComponentCompositeResponse;
import com.backend.application.dto.response.ComponentListItemResponse;
import com.backend.application.query.ComponentI18nQueries.GetComponentI18nQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.application.service.ComponentI18nService;
import com.backend.application.service.ComponentService;
import com.backend.application.service.ComponentTypeService;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentI18nRequest;
import com.backend.presentation.dto.request.BulkDeleteRequest;
import com.backend.presentation.dto.response.ComponentDetailResponse;
import com.backend.presentation.dto.response.ComponentI18nContentResponse;
import com.backend.presentation.dto.response.ComponentI18nResponse;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
public class ComponentController {

        private final ComponentService componentService;
        private final ComponentI18nService componentI18nService;
        private final ComponentTypeService componentTypeService;
        private final MessageSource messageSource;

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping
        public ResponseEntity<ApiResponse<ComponentResponse>> create(
                        @Valid @RequestBody ComponentCreateRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Component result = componentService.createComponent(request, userId);
                        ComponentResponse response = ComponentResponse.from(result);

                        String successMessage = messageSource.getMessage("component.create.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error creating component", ex);
                        String msg = messageSource.getMessage("component.create.error",
                                        null, Locale.forLanguageTag(lang));
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
                                Map<Component, List<ComponentI18n>> resultMap = componentService
                                                .getComponentWithI18n(id);

                                Map.Entry<Component, List<ComponentI18n>> entry = resultMap.entrySet().iterator()
                                                .next();
                                Component component = entry.getKey();
                                List<ComponentI18n> i18nList = entry.getValue();
                                Map<String, ComponentI18nContentResponse> translationsMap = i18nList.stream()
                                                .collect(Collectors.toMap(
                                                                i18n -> i18n.getLanguage().name(), // "TR", "EN", etc.
                                                                ComponentI18nContentResponse::from));

                                int publishedCount = (int) i18nList.stream()
                                                .filter(i18n -> com.backend.domain.enums.ComponentStatus.PUBLISHED
                                                                .equals(i18n.getStatus()))
                                                .count();
                                ComponentDetailResponse.Metadata metadata = new ComponentDetailResponse.Metadata(
                                                translationsMap.size(),
                                                publishedCount);

                                String componentTypeName = "";
                                try {
                                        ComponentType componentType = componentTypeService.getComponentTypeById(
                                                        new GetComponentTypeByIdQuery(component.getComponentTypeId()));
                                        componentTypeName = componentType.getUid();
                                } catch (Exception e) {
                                        log.warn("Could not fetch component type name for id: {}",
                                                        component.getComponentTypeId());
                                }

                                ComponentDetailResponse response = ComponentDetailResponse.from(
                                                component,
                                                componentTypeName,
                                                translationsMap,
                                                metadata);

                                return ResponseEntity.ok(ApiResponse.success(response));
                        }

                        Component result = componentService.getComponentById(id);
                        ComponentResponse response = ComponentResponse.from(result);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (Exception ex) {
                        log.error("Error getting component {}", id, ex);
                        String msg = messageSource.getMessage("component.get.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageableResponse<ComponentListItemResponse>>> list(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String search,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
                                        SortableFieldsConfig.COMPONENT_DEFAULT_SORT);
                        Sort sortObj = SortParseUtil.parse(effectiveSort,
                                        SortableFieldsConfig.COMPONENT_ALLOWED_FIELDS,
                                        SortableFieldsConfig.COMPONENT_DEFAULT_SORT);

                        PageRequest pageRequest = PageRequest.of(page, size, sortObj);
                        Page<ComponentListItemResponse> components = componentService.searchComponents(pageRequest, search);

                        SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.COMPONENT_SORT_OPTIONS);
                        PageableResponse<ComponentListItemResponse> response = PageableResponse.from(components, sortConfig);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (IllegalArgumentException ex) {
                        String message = messageSource.getMessage("component.sort.invalid",
                                        null,
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error listing components", ex);
                        String msg = messageSource.getMessage("component.list.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ComponentResponse>> update(
                        @PathVariable @NotNull @Min(1) Long id,
                        @Valid @RequestBody ComponentCreateRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
                        Component result = componentService.updateComponent(id, request, userId);
                        ComponentResponse response = ComponentResponse.from(result);

                        String successMessage = messageSource.getMessage("component.update.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error updating component {}", id, ex);
                        String msg = messageSource.getMessage("component.update.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        log.error("Error deleting component {}", id, ex);
                        String msg = messageSource.getMessage("component.delete.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/bulk-delete")
        public ResponseEntity<ApiResponse<BulkDeleteResultResponse>> bulkDelete(
                        @Valid @RequestBody BulkDeleteRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        BulkDeleteResultResponse result = componentService.bulkDeleteComponents(request.ids());
                        int requested = request.ids().size();
                        if (result.deletedIds().isEmpty() && result.failedIds().size() == requested && requested > 0) {
                                String allFailedMsg = messageSource.getMessage("component.bulk.delete.allFailed", null,
                                                Locale.forLanguageTag(lang));
                                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                                .body(ApiResponse.error(allFailedMsg));
                        }
                        String successMessage = messageSource.getMessage("component.bulk.delete.success",
                                        new Object[] { result.deletedIds().size(), result.failedIds().size() },
                                        Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, result));
                } catch (Exception ex) {
                        log.error("Error bulk deleting components", ex);
                        String msg = messageSource.getMessage("component.bulk.delete.error",
                                        null, Locale.forLanguageTag(lang));
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
                        GetComponentI18nQuery query = new GetComponentI18nQuery(id, language);
                        ComponentI18n result = componentI18nService.getComponentI18n(query);
                        ComponentI18nResponse response = ComponentI18nResponse.from(result);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (Exception ex) {
                        log.error("Error getting component i18n for component {} language {}", id, language, ex);
                        String msg = messageSource.getMessage("component.i18n.get.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}/i18n/{language}")
        public ResponseEntity<ApiResponse<ComponentI18nResponse>> upsertComponentI18n(
                        @PathVariable @NotNull @Min(1) Long id,
                        @PathVariable Language language,
                        @Valid @RequestBody ComponentI18nRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        UpsertComponentI18nCommand command = new UpsertComponentI18nCommand(
                                        id,
                                        language,
                                        request.title(),
                                        request.subtitle(),
                                        request.description(),
                                        request.status());

                        ComponentI18n result = componentI18nService.upsertComponentI18n(command);
                        ComponentI18nResponse response = ComponentI18nResponse.from(result);

                        String successMessage = messageSource.getMessage("component.i18n.upsert.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error upserting component i18n for component {} language {}", id, language, ex);
                        String msg = messageSource.getMessage("component.i18n.upsert.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/publish/{language}")
        public ResponseEntity<ApiResponse<ComponentI18nResponse>> publishComponentI18n(
                        @PathVariable @NotNull @Min(1) Long id,
                        @PathVariable Language language,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        PublishComponentI18nCommand command = new PublishComponentI18nCommand(id, language);
                        ComponentI18n result = componentI18nService.publishComponentI18n(command);
                        ComponentI18nResponse response = ComponentI18nResponse.from(result);

                        String successMessage = messageSource.getMessage("component.i18n.publish.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error publishing component i18n for component {} language {}", id, language, ex);
                        String msg = messageSource.getMessage("component.i18n.publish.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        // ==================== Composite Endpoints ====================

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/composite")
        public ResponseEntity<ApiResponse<ComponentCompositeResponse>> createComposite(
                        @Valid @RequestBody CreateComponentCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentCompositeResponse response = componentService.createComposite(request);

                        String successMessage = messageSource.getMessage("component.composite.create.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error creating component composite", ex);
                        String msg = messageSource.getMessage("component.composite.create.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}/composite")
        public ResponseEntity<ApiResponse<ComponentCompositeResponse>> updateComposite(
                        @PathVariable @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateComponentCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentCompositeResponse response = componentService.updateComposite(id, request);

                        String successMessage = messageSource.getMessage("component.composite.update.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error updating component composite {}", id, ex);
                        String msg = messageSource.getMessage("component.composite.update.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/{id}/composite")
        public ResponseEntity<ApiResponse<ComponentCompositeResponse>> getComposite(
                        @PathVariable @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        return componentService.getComposite(id)
                                        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                                        .orElseGet(() -> {
                                                String msg = messageSource.getMessage("component.not.found",
                                                                null, Locale.forLanguageTag(lang));
                                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                                .body(ApiResponse.error(msg));
                                        });
                } catch (Exception ex) {
                        log.error("Error getting component composite {}", id, ex);
                        String msg = messageSource.getMessage("component.composite.get.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(msg));
                }
        }

        // ==================== Responsive Media Endpoint ====================

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PatchMapping("/{id}/responsive-media")
        public ResponseEntity<ApiResponse<ComponentResponse>> assignResponsiveMedia(
                        @PathVariable @NotNull @Min(1) Long id,
                        @RequestParam(required = false) Long responsiveMediaId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                Component result = componentService.assignResponsiveMedia(id, responsiveMediaId);
                ComponentResponse response = ComponentResponse.from(result);

                String successMessage = messageSource.getMessage("component.responsive.assign.success",
                                null, Locale.forLanguageTag(lang));
                return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        }
}
