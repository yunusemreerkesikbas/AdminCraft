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

import com.backend.application.command.PageSlotCommands.AddComponentToSlotCommand;
import com.backend.application.command.PageSlotCommands.CreatePageSlotCommand;
import com.backend.application.command.PageSlotCommands.ReorderSlotComponentsCommand;
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

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class PageSlotController {

  private final PageSlotService pageSlotService;

  @GetMapping("/{pageId}/slots")
  public ResponseEntity<ApiResponse<List<PageSlotResponse>>> getSlots(
      @PathVariable @NotNull @Min(1) Long pageId) {
    List<PageSlotResponse> slots = pageSlotService.getSlotsByPageId(pageId);
    return ResponseEntity.ok(ApiResponse.success(slots));
  }

  @PostMapping("/{pageId}/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @Valid @RequestBody CreatePageSlotRequest request) {
    CreatePageSlotCommand command = mapToCommand(request);
    PageSlotResponse slot = pageSlotService.createSlot(pageId, command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Slot created successfully", slot));
  }

  @PostMapping("/shared/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSharedSlot(
      @Valid @RequestBody CreatePageSlotRequest request) {
    CreatePageSlotCommand command = new CreatePageSlotCommand(
        request.getSlotName(),
        request.getPosition(),
        request.getSortOrder(),
        true);
    PageSlotResponse slot = pageSlotService.createSlot(null, command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Shared slot created successfully", slot));
  }

  @GetMapping("/shared/slots")
  public ResponseEntity<ApiResponse<List<PageSlotResponse>>> getSharedSlots() {
    List<PageSlotResponse> slots = pageSlotService.getSharedSlots();
    return ResponseEntity.ok(ApiResponse.success(slots));
  }

  @DeleteMapping("/{pageId}/slots/{slotName}")
  public ResponseEntity<ApiResponse<Void>> deleteSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName) {
    pageSlotService.deleteSlot(pageId, slotName);
    return ResponseEntity.ok(ApiResponse.success("Slot deleted successfully", null));
  }

  @PostMapping("/{pageId}/slots/{slotName}/components")
  public ResponseEntity<ApiResponse<Void>> addComponentToSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @Valid @RequestBody AddComponentToSlotRequest request) {
    AddComponentToSlotCommand command = new AddComponentToSlotCommand(request.getComponentId());
    pageSlotService.addComponentToSlot(pageId, slotName, command);
    return ResponseEntity.ok(ApiResponse.success("Component added to slot", null));
  }

  @DeleteMapping("/{pageId}/slots/{slotName}/components/{componentId}")
  public ResponseEntity<ApiResponse<Void>> removeComponentFromSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @PathVariable @NotNull @Min(1) Long componentId) {
    pageSlotService.removeComponentFromSlot(pageId, slotName, componentId);
    return ResponseEntity.ok(ApiResponse.success("Component removed from slot", null));
  }

  @PutMapping("/{pageId}/slots/{slotName}/reorder")
  public ResponseEntity<ApiResponse<Void>> reorderComponents(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @Valid @RequestBody ReorderSlotComponentsRequest request) {
    ReorderSlotComponentsCommand command = new ReorderSlotComponentsCommand(request.getComponentIds());
    pageSlotService.reorderComponents(pageId, slotName, command);
    return ResponseEntity.ok(ApiResponse.success("Components reordered", null));
  }

  private CreatePageSlotCommand mapToCommand(CreatePageSlotRequest request) {
    return new CreatePageSlotCommand(
        request.getSlotName(),
        request.getPosition(),
        request.getSortOrder(),
        request.getIsShared());
  }
}
