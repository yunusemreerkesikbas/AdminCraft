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

  /**
   * Insert or update internationalization (i18n) data for a navigation node.
   *
   * @param id       the identifier of the navigation node to update (must be >= 1)
   * @param language the language of the i18n data
   * @param request  the i18n payload containing localized fields for the node
   * @return         an ApiResponse wrapping the updated NavigationNodeI18nResponse
   */
  @PutMapping("/nodes/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationNodeI18nResponse>> upsertNodeI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationNodeI18nRequest request) {
    NavigationNodeI18nResponse response = navigationI18nService.upsertNodeI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success("Node i18n updated successfully", response));
  }

  /**
   * Create a navigation node together with its translations.
   *
   * @param request the composite request containing node properties and their translations
   * @return a ResponseEntity whose body is an ApiResponse containing the created NavigationNodeCompositeResponse; returns HTTP 201 Created on success
   */
  @PostMapping("/nodes/composite")
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> createNodeComposite(
      @Valid @RequestBody CreateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.createNodeComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Node created with translations", response));
  }

  /**
   * Update a navigation node together with its translations.
   *
   * @param id      the identifier of the node to update (must be &gt;= 1)
   * @param request the update payload containing node fields and their translations
   * @return        an ApiResponse containing the updated NavigationNodeCompositeResponse
   */
  @PutMapping("/nodes/{id}/composite")
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> updateNodeComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.updateNodeComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success("Node updated with translations", response));
  }

  /**
   * Create a new navigation entry from the provided request.
   *
   * @param request the payload describing the navigation entry to create
   * @return a ResponseEntity containing an ApiResponse with the created NavigationEntryResponse and HTTP status 201 Created
   */
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

  /**
   * Upserts internationalized content for a navigation entry.
   *
   * @param id       the identifier of the navigation entry
   * @param language the language of the i18n data to insert or update
   * @param request  the localized fields for the entry
   * @return an ApiResponse wrapping the saved NavigationEntryI18nResponse and a success message
   */
  @PutMapping("/entries/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<NavigationEntryI18nResponse>> upsertEntryI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationEntryI18nRequest request) {
    NavigationEntryI18nResponse response = navigationI18nService.upsertEntryI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success("Entry i18n updated successfully", response));
  }

  /**
   * Create a navigation entry together with its translations.
   *
   * @param request the composite request containing entry data and its translations
   * @return an ApiResponse containing the created NavigationEntryCompositeResponse
   */
  @PostMapping("/entries/composite")
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> createEntryComposite(
      @Valid @RequestBody CreateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.createEntryComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Entry created with translations", response));
  }

  /**
   * Update a navigation entry together with its translations.
   *
   * @param id the identifier of the navigation entry to update; must be greater than or equal to 1
   * @param request the update payload including translated fields for the entry
   * @return an ApiResponse containing the updated NavigationEntryCompositeResponse with translations
   */
  @PutMapping("/entries/{id}/composite")
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> updateEntryComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.updateEntryComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success("Entry updated with translations", response));
  }
}