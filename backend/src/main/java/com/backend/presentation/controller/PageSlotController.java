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
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdatePageSlotRequest;
import com.backend.application.dto.slot.PageSlotDto;
import com.backend.application.dto.slot.SlotComponentDto;
import com.backend.application.service.PageSlotService;
import com.backend.presentation.dto.request.AddComponentToSlotRequest;
import com.backend.application.dto.request.CreatePageSlotRequest;
import com.backend.presentation.dto.response.PageSlotResponse;
import com.backend.presentation.dto.response.SlotComponentResponse;
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
    List<PageSlotDto> slots = pageSlotService.getSlotsByPageId(pageId);
    return ResponseEntity.ok(ApiResponse.success(mapToResponses(slots)));
  }

  @PostMapping("/{pageId}/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @Valid @RequestBody CreatePageSlotRequest request) {
    CreatePageSlotCommand command = mapToCommand(request);
    PageSlotDto slot = pageSlotService.createSlot(pageId, command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Slot created successfully", mapToResponse(slot)));
  }

  @PostMapping("/shared/slots")
  public ResponseEntity<ApiResponse<PageSlotResponse>> createSharedSlot(
      @Valid @RequestBody CreatePageSlotRequest request) {
    CreatePageSlotCommand command = new CreatePageSlotCommand(
      request.getUid(),
        request.getSlotName(),
        request.getPosition(),
        request.getSortOrder(),
        true);
    PageSlotDto slot = pageSlotService.createSlot(null, command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Shared slot created successfully", mapToResponse(slot)));
  }

  @GetMapping("/shared/slots")
  public ResponseEntity<ApiResponse<List<PageSlotResponse>>> getSharedSlots() {
    List<PageSlotDto> slots = pageSlotService.getSharedSlots();
    return ResponseEntity.ok(ApiResponse.success(mapToResponses(slots)));
  }

  @DeleteMapping("/{pageId}/slots/{slotName}")
  public ResponseEntity<ApiResponse<Void>> deleteSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName) {
    pageSlotService.deleteSlot(pageId, slotName);
    return ResponseEntity.ok(ApiResponse.success("Slot deleted successfully", null));
  }

  @PutMapping("/{pageId}/slots/{slotName}")
  public ResponseEntity<ApiResponse<PageSlotResponse>> updateSlot(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotBlank String slotName,
      @Valid @RequestBody UpdatePageSlotRequest request) {
    PageSlotDto slot = pageSlotService.updateSlot(pageId, slotName, request);
    return ResponseEntity.ok(ApiResponse.success("Slot updated successfully", mapToResponse(slot)));
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
      @Valid @RequestBody ReorderRequest<Long> request) {
    ReorderSlotComponentsCommand command = new ReorderSlotComponentsCommand(request.items());
    pageSlotService.reorderComponents(pageId, slotName, command);
    return ResponseEntity.ok(ApiResponse.success("Components reordered", null));
  }

  private CreatePageSlotCommand mapToCommand(CreatePageSlotRequest request) {
    return new CreatePageSlotCommand(
      request.getUid(),
        request.getSlotName(),
        request.getPosition(),
        request.getSortOrder(),
        request.getIsShared());
  }

  private List<PageSlotResponse> mapToResponses(List<PageSlotDto> dtos) {
    return dtos.stream().map(this::mapToResponse).toList();
  }

  private PageSlotResponse mapToResponse(PageSlotDto dto) {
    return PageSlotResponse.builder()
        .id(dto.id())
        .uid(dto.uid())
        .slotName(dto.slotName())
        .position(dto.position())
        .sortOrder(dto.sortOrder())
        .isActive(dto.isActive())
        .isShared(dto.isShared())
        .createdAt(dto.createdAt())
        .updatedAt(dto.updatedAt())
        .components(mapComponentsToResponses(dto.components()))
        .build();
  }

  private List<SlotComponentResponse> mapComponentsToResponses(List<SlotComponentDto> dtos) {
    if (dtos == null) {
      return List.of();
    }
    return dtos.stream().map(this::mapComponentToResponse).toList();
  }

  private SlotComponentResponse mapComponentToResponse(SlotComponentDto dto) {
    return SlotComponentResponse.builder()
        .id(dto.id())
        .componentId(dto.componentId())
        .componentUid(dto.componentUid())
        .componentName(dto.componentName())
        .componentTypeName(dto.componentTypeName())
        .sortOrder(dto.sortOrder())
        .isVisible(dto.isVisible())
        .build();
  }
}
