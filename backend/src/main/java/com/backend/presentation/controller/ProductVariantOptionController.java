package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.ProductVariantOptionService;
import com.backend.domain.enums.ProductVariantOptionDisplayType;
import com.backend.presentation.dto.request.CreateProductVariantOptionRequest;
import com.backend.presentation.dto.request.ProductVariantOptionValueRequest;
import com.backend.presentation.dto.request.UpdateProductVariantOptionRequest;
import com.backend.presentation.dto.response.ProductVariantOptionResponse;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products/variant-options")
@RequiredArgsConstructor
@Tag(name = "Product Variant Options", description = "Reusable product variant option definitions")
public class ProductVariantOptionController {

    private final ProductVariantOptionService optionService;
    private final MessageSource messageSource;

    @GetMapping
    @Operation(summary = "List product variant options")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<ProductVariantOptionResponse>>> list() {
		List<ProductVariantOptionResponse> response = optionService.findAll().stream()
				.map(ProductVariantOptionResponse::from)
				.toList();
		return ResponseEntity.ok(ApiResponse.success(message("product.variant.option.list.success"), response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product variant option")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> get(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				message("product.variant.option.get.success"),
				ProductVariantOptionResponse.from(optionService.findById(id))));
    }

    @PostMapping
    @Operation(summary = "Create product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> create(
            @Valid @RequestBody CreateProductVariantOptionRequest request) {
		ProductVariantOptionResponse response = ProductVariantOptionResponse.from(optionService.create(toApplicationRequest(request)));
        return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(message("product.variant.option.created"), response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductVariantOptionRequest request) {
		ProductVariantOptionResponse response = ProductVariantOptionResponse.from(optionService.update(id, toApplicationRequest(request)));
		return ResponseEntity.ok(ApiResponse.success(message("product.variant.option.updated"), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        optionService.delete(id);
		return ResponseEntity.ok(ApiResponse.success(message("product.variant.option.deleted"), null));
    }

    private com.backend.application.dto.request.CreateProductVariantOptionRequest toApplicationRequest(
			CreateProductVariantOptionRequest request) {
		return new com.backend.application.dto.request.CreateProductVariantOptionRequest(
				request.name(),
				displayType(request.displayType()),
				request.sortOrder(),
				request.active(),
				toApplicationValues(request.values()));
    }

    private com.backend.application.dto.request.UpdateProductVariantOptionRequest toApplicationRequest(
			UpdateProductVariantOptionRequest request) {
		return new com.backend.application.dto.request.UpdateProductVariantOptionRequest(
				request.name(),
				request.displayType() == null || request.displayType().isBlank() ? null : displayType(request.displayType()),
				request.sortOrder(),
				request.active(),
				request.values() == null ? null : toApplicationValues(request.values()));
    }

    private List<com.backend.application.dto.request.ProductVariantOptionValueRequest> toApplicationValues(
			List<ProductVariantOptionValueRequest> values) {
		return values.stream()
				.map(value -> new com.backend.application.dto.request.ProductVariantOptionValueRequest(
						value.id(),
						value.label(),
						value.swatchValue(),
						value.sortOrder(),
						value.active()))
				.toList();
    }

    private ProductVariantOptionDisplayType displayType(String value) {
		try {
			return ProductVariantOptionDisplayType.fromValue(value);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("validation.product.variant.option.displayType.invalid", ex);
		}
    }

    private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
    }
}
