package com.backend.presentation.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/navigation")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Navigation", description = "Navigation node and entry management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NavigationController {

  private final NavigationService navigationService;
  private final NavigationI18nService navigationI18nService;

  private final MessageSource messageSource;

  private String message(String key) {
    return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
  }

  // ==================== Node Endpoints ====================

  @GetMapping("/nodes")
  @Operation(summary = "List root nodes", description = "Returns paginated root navigation nodes")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Nodes returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<PageableResponse<NavigationNodeResponse>>> getRootNodes(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String search) {

    try {
      String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
          SortableFieldsConfig.NAVIGATION_NODE_DEFAULT_SORT);
      Sort sortObj = SortParseUtil.parse(effectiveSort, SortableFieldsConfig.NAVIGATION_NODE_ALLOWED_FIELDS,
          SortableFieldsConfig.NAVIGATION_NODE_DEFAULT_SORT);

      PageRequest pageRequest = PageRequest.of(page, size, sortObj);
      Page<NavigationNodeResponse> nodes = navigationService.searchRootNodes(pageRequest, search);

      SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.NAVIGATION_NODE_SORT_OPTIONS);


      PageableResponse<NavigationNodeResponse> response = PageableResponse.from(nodes, sortConfig);

      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message("navigation.sort.invalid")));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message("navigation.list.error")));
    }
  }

  @GetMapping("/nodes/{id}")
  @Operation(summary = "Get node by ID", description = "Returns a navigation node by identifier")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Node returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Node not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> getNodeById(
      @PathVariable @NotNull @Min(1) Long id) {
    return navigationService.getNodeById(id)
        .map(node -> ResponseEntity.ok(ApiResponse.success(node)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message("navigation.node.not.found"))));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/nodes")
  @Operation(summary = "Create root node", description = "Creates a new root navigation node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Node created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> createRootNode(
      @Valid @RequestBody CreateNodeRequest request) {
    NavigationNodeResponse node = navigationService.createRootNode(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message("navigation.node.created"), node));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/nodes/{id}/children")
  @Operation(summary = "Add child node", description = "Creates a child node under given parent node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Child node created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> addChildNode(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody CreateNodeRequest request) {
    NavigationNodeResponse child = navigationService.addChildNode(id, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message("navigation.node.child.added"), child));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/nodes/{id}")
  @Operation(summary = "Update node", description = "Updates navigation node fields")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Node updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeResponse>> updateNode(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateNodeRequest request) {
    NavigationNodeResponse node = navigationService.updateNode(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.node.updated"), node));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/nodes/{id}")
  @Operation(summary = "Delete node", description = "Deletes a navigation node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Node deleted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<Void>> deleteNode(
      @PathVariable @NotNull @Min(1) Long id) {
    navigationService.deleteNode(id);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.node.deleted"), null));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/nodes/{id}/reorder")
  @Operation(summary = "Reorder child nodes", description = "Reorders children under selected navigation node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Children reordered"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<Void>> reorderChildren(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody ReorderRequest<Long> request) {
    navigationService.reorderChildren(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.reorder.success"), null));
  }

  @GetMapping("/nodes/{id}/i18n/{language}")
  @Operation(summary = "Get node translation", description = "Returns translation of navigation node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeI18nResponse>> getNodeI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language) {
    NavigationNodeI18nResponse response = navigationI18nService.getNodeI18n(id, language);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/nodes/{id}/i18n/{language}")
  @Operation(summary = "Upsert node translation", description = "Creates or updates navigation node translation")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation upserted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeI18nResponse>> upsertNodeI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationNodeI18nRequest request) {
    NavigationNodeI18nResponse response = navigationI18nService.upsertNodeI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.node.i18n.updated"), response));
  }

  @GetMapping("/nodes/{id}/composite")
  @Operation(summary = "Get node composite", description = "Returns navigation node with translations")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Node not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> getNodeComposite(
      @PathVariable @NotNull @Min(1) Long id) {
    return navigationService.getNodeComposite(id)
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message("navigation.node.not.found"))));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/nodes/composite")
  @Operation(summary = "Create node composite", description = "Creates navigation node with translation payload")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Composite created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> createNodeComposite(
      @Valid @RequestBody CreateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.createNodeComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message("navigation.composite.node.created"), response));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/nodes/{id}/composite")
  @Operation(summary = "Update node composite", description = "Updates navigation node and translations")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationNodeCompositeResponse>> updateNodeComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateNodeCompositeRequest request) {
    NavigationNodeCompositeResponse response = navigationService.updateNodeComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.composite.node.updated"), response));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/entries")
  @Operation(summary = "Create entry", description = "Creates navigation entry under a node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Entry created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryResponse>> createEntry(
      @Valid @RequestBody CreateEntryRequest request) {
    NavigationEntryResponse entry = navigationService.createEntry(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message("navigation.entry.created"), entry));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/entries/{id}")
  @Operation(summary = "Update entry", description = "Updates navigation entry")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Entry updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryResponse>> updateEntry(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateEntryRequest request) {
    NavigationEntryResponse entry = navigationService.updateEntry(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.entry.updated"), entry));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/entries/{id}")
  @Operation(summary = "Delete entry", description = "Deletes navigation entry")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Entry deleted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<Void>> deleteEntry(
      @PathVariable @NotNull @Min(1) Long id) {
    navigationService.deleteEntry(id);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.entry.deleted"), null));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/nodes/{id}/entries/reorder")
  @Operation(summary = "Reorder entries", description = "Reorders entries under selected node")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Entries reordered"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<Void>> reorderEntries(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody ReorderRequest<Long> request) {
    navigationService.reorderEntries(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.reorder.success"), null));
  }

  @GetMapping("/entries/{id}/i18n/{language}")
  @Operation(summary = "Get entry translation", description = "Returns translation of navigation entry")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryI18nResponse>> getEntryI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language) {
    NavigationEntryI18nResponse response = navigationI18nService.getEntryI18n(id, language);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/entries/{id}/i18n/{language}")
  @Operation(summary = "Upsert entry translation", description = "Creates or updates navigation entry translation")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation upserted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryI18nResponse>> upsertEntryI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody NavigationEntryI18nRequest request) {
    NavigationEntryI18nResponse response = navigationI18nService.upsertEntryI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.entry.i18n.updated"), response));
  }

  @GetMapping("/entries/{id}/composite")
  @Operation(summary = "Get entry composite", description = "Returns navigation entry with translations")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entry not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> getEntryComposite(
      @PathVariable @NotNull @Min(1) Long id) {
    return navigationService.getEntryComposite(id)
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message("navigation.entry.not.found"))));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/entries/composite")
  @Operation(summary = "Create entry composite", description = "Creates navigation entry with translation payload")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Composite created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> createEntryComposite(
      @Valid @RequestBody CreateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.createEntryComposite(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(message("navigation.composite.entry.created"), response));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/entries/{id}/composite")
  @Operation(summary = "Update entry composite", description = "Updates navigation entry and translations")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<NavigationEntryCompositeResponse>> updateEntryComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdateEntryCompositeRequest request) {
    NavigationEntryCompositeResponse response = navigationService.updateEntryComposite(id, request);
    return ResponseEntity.ok(ApiResponse.success(message("navigation.composite.entry.updated"), response));
  }
}
