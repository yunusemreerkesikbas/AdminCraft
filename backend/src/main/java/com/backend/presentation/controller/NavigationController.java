package com.backend.presentation.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.CreateEntryRequest;
import com.backend.application.dto.request.CreateNodeRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdateEntryRequest;
import com.backend.application.dto.request.UpdateNodeRequest;
import com.backend.application.dto.response.NavigationEntryResponse;
import com.backend.application.dto.response.NavigationNodeResponse;
import com.backend.application.service.NavigationService;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/navigation")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class NavigationController {

  private final NavigationService navigationService;

  // ==================== Node Endpoints ====================

  @GetMapping("/nodes")
  public ResponseEntity<ApiResponse<List<NavigationNodeResponse>>> getRootNodes() {
    List<NavigationNodeResponse> nodes = navigationService.getRootNodes();
    return ResponseEntity.ok(ApiResponse.success(nodes));
  }

  @GetMapping("/nodes/{id}")
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> getNodeById(
      @PathVariable @NotNull @Min(1) Long id) {
    return navigationService.getNodeById(id)
        .map(node -> ResponseEntity.ok(ApiResponse.success(node)))
        .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Navigation node not found")));
  }

  @PostMapping("/nodes")
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> createRootNode(
      @Valid @RequestBody CreateNodeRequest request) {
    NavigationNodeResponse node = navigationService.createRootNode(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Navigation node created successfully", node));
  }

  @PostMapping("/nodes/{id}/children")
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> addChildNode(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody CreateNodeRequest request) {
    NavigationNodeResponse child = navigationService.addChildNode(id, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Child node added successfully", child));
  }

  @PutMapping("/nodes/{id}")
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> updateNode(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateNodeRequest request) {
    NavigationNodeResponse node = navigationService.updateNode(id, request);
    return ResponseEntity.ok(ApiResponse.success("Navigation node updated successfully", node));
  }

  @DeleteMapping("/nodes/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteNode(
      @PathVariable @NotNull @Min(1) Long id) {
    navigationService.deleteNode(id);
    return ResponseEntity.ok(ApiResponse.success("Navigation node deleted successfully", null));
  }

  @PutMapping("/nodes/{id}/reorder")
  public ResponseEntity<ApiResponse<Void>> reorderChildren(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody ReorderRequest<Long> request) {
    navigationService.reorderChildren(id, request);
    return ResponseEntity.ok(ApiResponse.success("Children reordered successfully", null));
  }

  // ==================== Entry Endpoints ====================

  @PostMapping("/entries")
  public ResponseEntity<ApiResponse<NavigationEntryResponse>> createEntry(
      @Valid @RequestBody CreateEntryRequest request) {
    NavigationEntryResponse entry = navigationService.createEntry(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Navigation entry created successfully", entry));
  }

  @PutMapping("/entries/{id}")
  public ResponseEntity<ApiResponse<NavigationEntryResponse>> updateEntry(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateEntryRequest request) {
    NavigationEntryResponse entry = navigationService.updateEntry(id, request);
    return ResponseEntity.ok(ApiResponse.success("Navigation entry updated successfully", entry));
  }

  @DeleteMapping("/entries/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteEntry(
      @PathVariable @NotNull @Min(1) Long id) {
    navigationService.deleteEntry(id);
    return ResponseEntity.ok(ApiResponse.success("Navigation entry deleted successfully", null));
  }

  @PutMapping("/nodes/{id}/entries/reorder")
  public ResponseEntity<ApiResponse<Void>> reorderEntries(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody ReorderRequest<Long> request) {
    navigationService.reorderEntries(id, request);
    return ResponseEntity.ok(ApiResponse.success("Entries reordered successfully", null));
  }
}
