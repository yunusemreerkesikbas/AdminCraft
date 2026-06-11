package com.backend.presentation.controller;

import java.util.List;

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

import com.backend.application.dto.request.CreateProductVariantOptionRequest;
import com.backend.application.dto.request.UpdateProductVariantOptionRequest;
import com.backend.application.dto.response.ProductVariantOptionResponse;
import com.backend.application.service.ProductVariantOptionService;
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

    @GetMapping
    @Operation(summary = "List product variant options")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<ProductVariantOptionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(optionService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product variant option")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(optionService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> create(
            @Valid @RequestBody CreateProductVariantOptionRequest request) {
        ProductVariantOptionResponse response = optionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product variant option created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantOptionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductVariantOptionRequest request) {
        ProductVariantOptionResponse response = optionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product variant option updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product variant option")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        optionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product variant option deleted successfully", null));
    }
}
