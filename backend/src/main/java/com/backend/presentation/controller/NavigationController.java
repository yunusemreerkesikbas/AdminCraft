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

import com.backend.application.dto.request.CreateEntryCompositeRequest;
import com.backend.application.dto.request.CreateEntryRequest;
import com.backend.application.dto.request.CreateNodeCompositeRequest;
import com.backend.application.dto.request.CreateNodeRequest;
import com.backend.application.dto.request.NavigationEntryI18nRequest;
import com.backend.application.dto.request.NavigationNodeI18nRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdateEntryCompositeRequest;
import com.backend.application.dto.request.UpdateEntryRequest;
import com.backend.application.dto.request.UpdateNodeCompositeRequest;
import com.backend.application.dto.request.UpdateNodeRequest;
import com.backend.application.dto.response.NavigationEntryCompositeResponse;
import com.backend.application.dto.response.NavigationEntryI18nResponse;
import com.backend.application.dto.response.NavigationEntryResponse;
import com.backend.application.dto.response.NavigationNodeCompositeResponse;
import com.backend.application.dto.response.NavigationNodeI18nResponse;
import com.backend.application.dto.response.NavigationNodeResponse;
import com.backend.application.service.NavigationI18nService;
import com.backend.application.service.NavigationService;
import com.backend.domain.enums.Language;
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
  private final NavigationI18nService navigationI18nService;

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

  @GetMapping("/nodes/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationNodeI18nResponse>> getNodeI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language) {
    NavigationNodeI18nResponse response = navigationI18nService.getNodeI18n(id, language);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/nodes/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationNodeI18nResponse>> upsertNodeI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationNodeI18nRequest request) {
    NavigationNodeI18nResponse response = navigationI18nService.upsertNodeI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success("Node i18n updated successfully", response));
  }

  @PostMapping("/nodes/composite")
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> createNodeComposite(
      @Valid @RequestBody CreateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.createNodeComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Node created with translations", response));
  }

  @PutMapping("/nodes/{id}/composite")
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> updateNodeComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.updateNodeComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success("Node updated with translations", response));
  }

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

  @GetMapping("/entries/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationEntryI18nResponse>> getEntryI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language) {
    NavigationEntryI18nResponse response = navigationI18nService.getEntryI18n(id, language);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/entries/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationEntryI18nResponse>> upsertEntryI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationEntryI18nRequest request) {
    NavigationEntryI18nResponse response = navigationI18nService.upsertEntryI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success("Entry i18n updated successfully", response));
  }

  @PostMapping("/entries/composite")
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> createEntryComposite(
      @Valid @RequestBody CreateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.createEntryComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Entry created with translations", response));
  }

  @PutMapping("/entries/{id}/composite")
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> updateEntryComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.updateEntryComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success("Entry updated with translations", response));
  }
}
