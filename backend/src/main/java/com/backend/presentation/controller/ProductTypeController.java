package com.backend.presentation.controller;

import com.backend.application.service.ProductTypeService;
import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.presentation.dto.request.AttributeDefinitionCreateRequest;
import com.backend.presentation.dto.request.AttributeDefinitionUpdateRequest;
import com.backend.presentation.dto.request.ProductTypeCreateRequest;
import com.backend.presentation.dto.request.ProductTypeUpdateRequest;
import com.backend.presentation.dto.response.AttributeDefinitionResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.ProductTypeDetailResponse;
import com.backend.presentation.dto.response.ProductTypeResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import com.backend.shared.common.SortParseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.backend.presentation.dto.response.SortOptionDto;

@RestController
@RequestMapping("/products/types")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Product Type", description = "Endpoints for managing product types and their attribute definitions")
public class ProductTypeController {

    private final ProductTypeService productTypeService;
    private final MessageSource messageSource;

    @GetMapping
    @Operation(summary = "List product types", description = "Retrieves all product types with pagination and search")
    public ResponseEntity<ApiResponse<PageableResponse<ProductTypeResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            String effectiveSort = sort != null ? sort : "name,asc";
            Sort sortObj = SortParseUtil.parse(effectiveSort, Set.of("name", "code", "category", "createdAt"), "name,asc");
            PageRequest pageRequest = PageRequest.of(page, size, sortObj);

            Page<ProductType> types = productTypeService.search(search, pageRequest);
            Page<ProductTypeResponse> responsePage = types.map(ProductTypeResponse::fromWithoutCount);

            SortConfig sortConfig = SortConfig.of(effectiveSort, List.of(
                    SortOptionDto.defaultOption("name,asc", "admin.sort.name"),
                    SortOptionDto.of("code,asc", "admin.sort.code"),
                    SortOptionDto.of("createdAt,desc", "admin.sort.newest")
            ));
            PageableResponse<ProductTypeResponse> response = PageableResponse.from(responsePage, sortConfig);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error listing product types: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.type.list.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product type by ID", description = "Retrieves a product type with its attribute definitions")
    public ResponseEntity<ApiResponse<ProductTypeDetailResponse>> getById(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            return productTypeService.findByIdWithAttributes(id)
                    .map(pt -> ResponseEntity.ok(ApiResponse.success(ProductTypeDetailResponse.from(pt))))
                    .orElseGet(() -> {
                        String msg = messageSource.getMessage("product.type.not.found",
                                new Object[]{id}, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
                    });
        } catch (Exception ex) {
            log.error("Error getting product type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("product.type.get.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @PostMapping
    @Operation(summary = "Create product type", description = "Creates a new product type")
    public ResponseEntity<ApiResponse<ProductTypeResponse>> create(
            @RequestBody @Valid ProductTypeCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            ProductType created = productTypeService.create(
                    request.code(), request.name(), request.category(), userId);

            String msg = messageSource.getMessage("product.type.create.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(msg, ProductTypeResponse.fromWithoutCount(created)));
        } catch (IllegalArgumentException ex) {
            log.warn("Product type creation validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.type.create.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error creating product type: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.type.create.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product type", description = "Updates an existing product type")
    public ResponseEntity<ApiResponse<ProductTypeResponse>> update(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestBody @Valid ProductTypeUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            Long userId = SecurityUtil.getCurrentUserIdOrThrow();
            ProductType updated = productTypeService.update(id, request.name(), request.category(), userId);

            String msg = messageSource.getMessage("product.type.update.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, ProductTypeResponse.fromWithoutCount(updated)));
        } catch (IllegalArgumentException ex) {
            log.warn("Product type update validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.type.update.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error updating product type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("product.type.update.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product type", description = "Deletes a product type and all its attributes")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            productTypeService.delete(id);
            String msg = messageSource.getMessage("product.type.delete.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, null));
        } catch (IllegalArgumentException ex) {
            log.warn("Product type delete validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.type.delete.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error deleting product type {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("product.type.delete.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{typeId}/attributes")
    @Operation(summary = "List attributes", description = "Retrieves all attribute definitions for a product type")
    public ResponseEntity<ApiResponse<List<AttributeDefinitionResponse>>> listAttributes(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long typeId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            List<ProductAttributeDefinition> attributes = productTypeService.getAttributes(typeId);
            List<AttributeDefinitionResponse> response = attributes.stream()
                    .map(AttributeDefinitionResponse::from)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error listing attributes for product type {}: {}", typeId, ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.list.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(msg));
        }
    }

    @PostMapping("/{typeId}/attributes")
    @Operation(summary = "Create attribute", description = "Adds a new attribute definition to a product type")
    public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> createAttribute(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long typeId,
            @RequestBody @Valid AttributeDefinitionCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            ProductAttributeDefinition created = productTypeService.addAttribute(
                    typeId, request.code(), request.name(), request.fieldType(),
                    request.isRequired(), request.isSearchable(), request.sortOrder(),
                    request.validationConfig());

            String msg = messageSource.getMessage("product.attribute.create.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(msg, AttributeDefinitionResponse.from(created)));
        } catch (IllegalArgumentException ex) {
            log.warn("Attribute creation validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.create.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error creating attribute for product type {}: {}", typeId, ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.create.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @PutMapping("/{typeId}/attributes/{attrId}")
    @Operation(summary = "Update attribute", description = "Updates an existing attribute definition")
    public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> updateAttribute(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long typeId,
            @Parameter(description = "Attribute ID") @PathVariable @Valid @NotNull @Min(1) Long attrId,
            @RequestBody @Valid AttributeDefinitionUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            ProductAttributeDefinition updated = productTypeService.updateAttribute(
                    typeId, attrId, request.name(), request.fieldType(),
                    request.isRequired(), request.isSearchable(), request.sortOrder(),
                    request.validationConfig());

            String msg = messageSource.getMessage("product.attribute.update.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, AttributeDefinitionResponse.from(updated)));
        } catch (IllegalArgumentException ex) {
            log.warn("Attribute update validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.update.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error updating attribute {} for product type {}: {}", attrId, typeId, ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.update.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{typeId}/attributes/{attrId}")
    @Operation(summary = "Delete attribute", description = "Deletes an attribute definition from a product type")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(
            @Parameter(description = "Product Type ID") @PathVariable @Valid @NotNull @Min(1) Long typeId,
            @Parameter(description = "Attribute ID") @PathVariable @Valid @NotNull @Min(1) Long attrId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            productTypeService.deleteAttribute(typeId, attrId);
            String msg = messageSource.getMessage("product.attribute.delete.success", null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(msg, null));
        } catch (IllegalArgumentException ex) {
            log.warn("Attribute delete validation error: {}", ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.delete.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
        } catch (Exception ex) {
            log.error("Error deleting attribute {} for product type {}: {}", attrId, typeId, ex.getMessage());
            String msg = messageSource.getMessage("product.attribute.delete.error",
                    new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
    }
}
