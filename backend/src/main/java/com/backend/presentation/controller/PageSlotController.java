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

import com.backend.application.service.PageSlotService;
import com.backend.presentation.dto.request.AddComponentToSlotRequest;
import com.backend.presentation.dto.request.CreatePageSlotRequest;
import com.backend.presentation.dto.request.ReorderSlotComponentsRequest;
import com.backend.presentation.dto.response.PageSlotResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class PageSlotController {

  private final PageSlotService pageSlotService;

  @GetMapping("/{pageId}/slots")
  public ResponseEntity<ApiResponse<List<PageSlotResponse>>> getSlots(
      @PathVariable @NotNull @Min(1) Long pageId) {
    try {
      List<PageSlotResponse> slots = pageSlotService.getSlotsByPageId(pageId);
      return ResponseEntity.ok(ApiResponse.success(slots));
    } catch (IllegalArgumentException ex) {
      log.warn("Error getting slots for page {}: {}", pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error getting slots for page {}: {}", pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get slots"));
    }
  }

  @PostMapping("/{pageId}/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @Valid @RequestBody CreatePageSlotRequest request) {
    try {
      PageSlotResponse slot = pageSlotService.createSlot(pageId, request);
      log.info("Created slot '{}' for page {}", request.getSlotName(), pageId);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success("Slot created successfully", slot));
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error creating slot for page {}: {}", pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error creating slot for page {}: {}", pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to create slot"));
    }
  }

  @PostMapping("/shared/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSharedSlot(
      @Valid @RequestBody CreatePageSlotRequest request) {
    try {
      request.setIsShared(true);
      PageSlotResponse slot = pageSlotService.createSlot(null, request);
      log.info("Created shared slot '{}'", request.getSlotName());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success("Shared slot created successfully", slot));
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error creating shared slot: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error creating shared slot: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to create shared slot"));
    }
  }

  @GetMapping("/shared/slots")
  public ResponseEntity<ApiResponse<List<PageSlotResponse>>> getSharedSlots() {
    try {
      List<PageSlotResponse> slots = pageSlotService.getSharedSlots();
      return ResponseEntity.ok(ApiResponse.success(slots));
    } catch (Exception ex) {
      log.error("Error getting shared slots: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get shared slots"));
    }
  }

  @DeleteMapping("/{pageId}/slots/{slotName}")
  public ResponseEntity<ApiResponse<Void>> deleteSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName) {
    try {
      pageSlotService.deleteSlot(pageId, slotName);
      log.info("Deleted slot '{}' from page {}", slotName, pageId);
      return ResponseEntity.ok(ApiResponse.success("Slot deleted successfully", null));
    } catch (IllegalArgumentException ex) {
      log.warn("Error deleting slot '{}' from page {}: {}", slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error deleting slot '{}' from page {}: {}", slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to delete slot"));
    }
  }

  @PostMapping("/{pageId}/slots/{slotName}/components")
  public ResponseEntity<ApiResponse<Void>> addComponentToSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @Valid @RequestBody AddComponentToSlotRequest request) {
    try {
      pageSlotService.addComponentToSlot(pageId, slotName, request.getComponentId());
      log.info("Added component {} to slot '{}' in page {}",
          request.getComponentId(), slotName, pageId);
      return ResponseEntity.ok(ApiResponse.success("Component added to slot", null));
    } catch (IllegalArgumentException ex) {
      log.warn("Error adding component to slot '{}' in page {}: {}",
          slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error adding component to slot '{}' in page {}: {}",
          slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to add component to slot"));
    }
  }

  @DeleteMapping("/{pageId}/slots/{slotName}/components/{componentId}")
  public ResponseEntity<ApiResponse<Void>> removeComponentFromSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @PathVariable @NotNull @Min(1) Long componentId) {
    try {
      pageSlotService.removeComponentFromSlot(pageId, slotName, componentId);
      log.info("Removed component {} from slot '{}' in page {}",
          componentId, slotName, pageId);
      return ResponseEntity.ok(ApiResponse.success("Component removed from slot", null));
    } catch (IllegalArgumentException ex) {
      log.warn("Error removing component {} from slot '{}' in page {}: {}",
          componentId, slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error removing component {} from slot '{}' in page {}: {}",
          componentId, slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to remove component from slot"));
    }
  }

  @PutMapping("/{pageId}/slots/{slotName}/reorder")
  public ResponseEntity<ApiResponse<Void>> reorderComponents(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @Valid @RequestBody ReorderSlotComponentsRequest request) {
    try {
      pageSlotService.reorderComponents(pageId, slotName, request.getComponentIds());
      log.info("Reordered components in slot '{}' for page {}", slotName, pageId);
      return ResponseEntity.ok(ApiResponse.success("Components reordered", null));
    } catch (IllegalArgumentException ex) {
      log.warn("Error reordering components in slot '{}' for page {}: {}",
          slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error reordering components in slot '{}' for page {}: {}",
          slotName, pageId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to reorder components"));
    }
  }
}
