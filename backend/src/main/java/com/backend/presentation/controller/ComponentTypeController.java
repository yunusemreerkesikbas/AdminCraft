package com.backend.presentation.controller;

import com.backend.application.command.ComponentTypeCommands.*;
import com.backend.application.query.ComponentTypeQueries.*;
import com.backend.application.service.ComponentTypeService;
import com.backend.domain.entity.ComponentType;
import com.backend.presentation.dto.request.ComponentTypeCreateRequest;
import com.backend.presentation.dto.response.ComponentTypeResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/components/types")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class ComponentTypeController {

    private final ComponentTypeService componentTypeService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> create(
            @Valid @RequestBody ComponentTypeCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();

            CreateComponentTypeCommand command = new CreateComponentTypeCommand(
                    request.code(),
                    request.name(),
                    request.category(),
                    request.icon(),
                    request.extendedFieldsSchema(),
                    userId);

            ComponentType result = componentTypeService.createComponentType(command);
            ComponentTypeResponse response = ComponentTypeResponse.from(result);

            String successMessage = messageSource.getMessage("component.type.create.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error creating component type: {}", ex.getMessage());
            String msg = messageSource.getMessage("component.type.create.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> getById(
            @PathVariable @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            GetComponentTypeByIdQuery query = new GetComponentTypeByIdQuery(id);
            ComponentType result = componentTypeService.getComponentTypeById(query);
            ComponentTypeResponse response = ComponentTypeResponse.from(result);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting component type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.type.get.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponentTypeResponse>>> list(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            GetAllComponentTypesQuery query = new GetAllComponentTypesQuery();
            List<ComponentType> types = componentTypeService.getAllComponentTypes(query);
            List<ComponentTypeResponse> responses = types.stream()
                    .map(ComponentTypeResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            log.error("Error listing component types: {}", ex.getMessage());
            String msg = messageSource.getMessage("component.type.list.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> update(
            @PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody ComponentTypeCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();

            UpdateComponentTypeCommand command = new UpdateComponentTypeCommand(
                    id,
                    request.code(),
                    request.name(),
                    request.category(),
                    request.icon(),
                    request.extendedFieldsSchema(),
                    userId);

            ComponentType result = componentTypeService.updateComponentType(command);
            ComponentTypeResponse response = ComponentTypeResponse.from(result);

            String successMessage = messageSource.getMessage("component.type.update.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error updating component type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.type.update.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            DeleteComponentTypeCommand command = new DeleteComponentTypeCommand(id);
            componentTypeService.deleteComponentType(command);

            String successMessage = messageSource.getMessage("component.type.delete.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, null));
        } catch (Exception ex) {
            log.error("Error deleting component type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("component.type.delete.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }

    @PostMapping("/validate-schema")
    public ResponseEntity<ApiResponse<JsonNode>> validateSchema(
            @RequestBody JsonNode schema,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            JsonNode sanitizedSchema = componentTypeService.validateSchema(schema);
            String successMessage = messageSource.getMessage("component.type.schema.validate.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, sanitizedSchema));
        } catch (Exception ex) {
            log.error("Schema validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("component.type.schema.validate.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }
}
