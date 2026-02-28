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

import com.backend.application.dto.request.CreateProductFieldRequest;
import com.backend.application.dto.request.UpdateProductFieldRequest;
import com.backend.application.dto.response.ProductFieldDefinitionResponse;
import com.backend.application.service.ProductFieldService;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing global product field definitions.
 */
@RestController
@RequestMapping("/products/fields")
@RequiredArgsConstructor
@Tag(name = "Product Fields", description = "Global product field definitions API")
public class ProductFieldController {

  private final ProductFieldService productFieldService;

  @GetMapping
  @Operation(summary = "Get all field definitions")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
  public ResponseEntity<ApiResponse<List<ProductFieldDefinitionResponse>>> getAllDefinitions() {
    List<ProductFieldDefinitionResponse> definitions = productFieldService.findAllDefinitions();
    return ResponseEntity.ok(ApiResponse.success(definitions));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get field definition by ID")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
  public ResponseEntity<ApiResponse<ProductFieldDefinitionResponse>> getDefinitionById(@PathVariable Long id) {
    ProductFieldDefinitionResponse definition = productFieldService.findDefinitionById(id);
    return ResponseEntity.ok(ApiResponse.success(definition));
  }

  @PostMapping
  @Operation(summary = "Create a new field definition")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<ProductFieldDefinitionResponse>> createDefinition(
      @Valid @RequestBody CreateProductFieldRequest request) {
    ProductFieldDefinitionResponse definition = productFieldService.createDefinition(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Product field created successfully", definition));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a field definition")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<ProductFieldDefinitionResponse>> updateDefinition(
      @PathVariable Long id,
      @Valid @RequestBody UpdateProductFieldRequest request) {
    ProductFieldDefinitionResponse definition = productFieldService.updateDefinition(id, request);
    return ResponseEntity.ok(ApiResponse.success("Product field updated successfully", definition));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a field definition")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteDefinition(@PathVariable Long id) {
    productFieldService.deleteDefinition(id);
    return ResponseEntity.ok(ApiResponse.success("Product field deleted successfully", null));
  }
}
