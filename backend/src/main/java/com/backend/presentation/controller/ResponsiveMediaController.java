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

import com.backend.application.dto.request.ResponsiveMediaRequest;
import com.backend.application.dto.response.ResponsiveMediaResponse;
import com.backend.application.service.ResponsiveMediaService;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for ResponsiveMediaSet operations.
 */
@RestController
@RequestMapping("/responsive-media")
@RequiredArgsConstructor
@Tag(name = "Responsive Media", description = "Desktop/Mobile responsive media set management")
public class ResponsiveMediaController {

  private final ResponsiveMediaService service;

  @PostMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Create responsive media set", description = "Create a new Desktop/Mobile media pair")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Code already exists")
  })
  public ResponseEntity<ApiResponse<ResponsiveMediaResponse>> create(
      @Valid @RequestBody ResponsiveMediaRequest request) {
    ResponsiveMediaResponse response = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Get responsive media set by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
  })
  public ResponseEntity<ApiResponse<ResponsiveMediaResponse>> getById(
      @Parameter(description = "Responsive media set ID") @PathVariable Long id) {
    ResponsiveMediaResponse response = service.getById(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/uid/{uid}")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Get responsive media set by UID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
  })
  public ResponseEntity<ApiResponse<ResponsiveMediaResponse>> getByUid(
      @Parameter(description = "Responsive media set UID") @PathVariable String uid) {
    ResponsiveMediaResponse response = service.getByUid(uid);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Update responsive media set", description = "Update desktop/mobile media references")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
  })
  public ResponseEntity<ApiResponse<ResponsiveMediaResponse>> update(
      @Parameter(description = "Responsive media set ID") @PathVariable Long id,
      @Valid @RequestBody ResponsiveMediaRequest request) {
    ResponsiveMediaResponse response = service.update(id, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Delete responsive media set")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
  })
  public ResponseEntity<Void> delete(
      @Parameter(description = "Responsive media set ID") @PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/media/{mediaId}/linked-components")
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @Operation(summary = "Get components using a media", description = "Returns component IDs that use the specified media")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of component IDs")
  })
  public ResponseEntity<ApiResponse<List<Long>>> getLinkedComponents(
      @Parameter(description = "Media ID") @PathVariable Long mediaId) {
    List<Long> componentIds = service.getLinkedComponentIds(mediaId);
    return ResponseEntity.ok(ApiResponse.success(componentIds));
  }
}
