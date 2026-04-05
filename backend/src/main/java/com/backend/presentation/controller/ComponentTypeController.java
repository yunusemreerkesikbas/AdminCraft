package com.backend.presentation.controller;

import java.util.Locale;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.application.service.ComponentTypeService;
import com.backend.domain.entity.ComponentType;
import com.backend.presentation.dto.request.ComponentTypeCreateRequest;
import com.backend.presentation.dto.response.ComponentTypeResponse;
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
@RequestMapping("/components/types")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
public class ComponentTypeController {

    private final ComponentTypeService componentTypeService;
    private final MessageSource messageSource;

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> create(
            @Valid @RequestBody ComponentTypeCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();

        ComponentType result = componentTypeService.createComponentType(
                request.name(),
                request.category(),
                request.navigationAware(),
                userId);
        ComponentTypeResponse response = ComponentTypeResponse.from(result);

        String successMessage = messageSource.getMessage("component.type.create.success",
                null, Locale.forLanguageTag(lang));
        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> getById(
            @PathVariable @NotNull @Min(1) Long id) {
        GetComponentTypeByIdQuery query = new GetComponentTypeByIdQuery(id);
        ComponentType result = componentTypeService.getComponentTypeById(query);
        ComponentTypeResponse response = ComponentTypeResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageableResponse<ComponentTypeResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
                    SortableFieldsConfig.COMPONENT_TYPE_DEFAULT_SORT);
            Sort sortObj = SortParseUtil.parse(effectiveSort,
                    SortableFieldsConfig.COMPONENT_TYPE_ALLOWED_FIELDS,
                    SortableFieldsConfig.COMPONENT_TYPE_DEFAULT_SORT);

            PageRequest pageRequest = PageRequest.of(page, size, sortObj);
            Page<ComponentType> types = componentTypeService.searchComponentTypes(pageRequest, search);
            Page<ComponentTypeResponse> responsePage = types.map(ComponentTypeResponse::from);

            SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.COMPONENT_TYPE_SORT_OPTIONS);
            PageableResponse<ComponentTypeResponse> response = PageableResponse.from(responsePage, sortConfig);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException ex) {
            String message = messageSource.getMessage("component.type.sort.invalid",
                    null,
                    Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error listing component types", ex);
            String msg = messageSource.getMessage("component.type.list.error",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> update(
            @PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody ComponentTypeCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();

        ComponentType result = componentTypeService.updateComponentType(
                id,
                request.name(),
                request.category(),
                request.navigationAware(),
                userId);
        ComponentTypeResponse response = ComponentTypeResponse.from(result);

        String successMessage = messageSource.getMessage("component.type.update.success",
                null, Locale.forLanguageTag(lang));
        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        componentTypeService.deleteComponentType(id);

        String successMessage = messageSource.getMessage("component.type.delete.success",
                null, Locale.forLanguageTag(lang));
        return ResponseEntity.ok(ApiResponse.success(successMessage, null));
    }
}
