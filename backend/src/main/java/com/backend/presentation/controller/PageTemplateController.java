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

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.request.CreatePageTemplateRequest;
import com.backend.application.dto.request.CreateTemplateSlotRequest;
import com.backend.application.dto.request.PageTemplateI18nRequest;
import com.backend.application.dto.request.ReorderRequest;
import com.backend.application.dto.request.UpdatePageTemplateRequest;
import com.backend.application.dto.response.PageTemplateI18nResponse;
import com.backend.application.dto.template.PageTemplateDto;
import com.backend.application.dto.template.TemplateSlotDto;
import com.backend.application.service.PageTemplateI18nService;
import com.backend.application.service.PageTemplateService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.PageTemplateResponse;
import com.backend.presentation.dto.response.TemplateSlotResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/page-templates")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class PageTemplateController {

  private final PageTemplateService pageTemplateService;
  private final PageTemplateI18nService pageTemplateI18nService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageTemplateResponse>>> getAllTemplates() {
    List<PageTemplateDto> templates = pageTemplateService.getAll();
    return ResponseEntity.ok(ApiResponse.success(mapToResponses(templates)));
  }

  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<PageTemplateResponse>>> getActiveTemplates() {
    List<PageTemplateDto> templates = pageTemplateService.getActiveTemplates();
    return ResponseEntity.ok(ApiResponse.success(mapToResponses(templates)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PageTemplateResponse>> getTemplateById(
      @PathVariable @NotNull @Min(1) Long id) {
    PageTemplateDto template = pageTemplateService.getById(id);
    return ResponseEntity.ok(ApiResponse.success(mapToResponse(template)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PageTemplateResponse>> createTemplate(
      @Valid @RequestBody CreatePageTemplateRequest request) {
    CreatePageTemplateCommand command = new CreatePageTemplateCommand(
        request.getName(),
        request.getUid(),
        request.getDescription(),
        request.getIsActive());

    PageTemplateDto template = pageTemplateService.create(command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Template created successfully", mapToResponse(template)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PageTemplateResponse>> updateTemplate(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdatePageTemplateRequest request) {
    UpdatePageTemplateCommand command = new UpdatePageTemplateCommand(
        request.getName(),
        request.getDescription(),
        request.getIsActive());

    PageTemplateDto template = pageTemplateService.update(id, command);
    return ResponseEntity.ok(ApiResponse.success("Template updated successfully", mapToResponse(template)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteTemplate(
      @PathVariable @NotNull @Min(1) Long id) {
    pageTemplateService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
  }

  @PostMapping("/{id}/slots")
  public ResponseEntity<ApiResponse<TemplateSlotResponse>> addSlot(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody CreateTemplateSlotRequest request) {
    CreateTemplateSlotCommand command = new CreateTemplateSlotCommand(
        request.getSlotName(),
        request.getPosition(),
        request.getSortOrder(),
        request.getIsRequired(),
        request.getMaxComponents(),
        request.getAllowedTypes());

    TemplateSlotDto slot = pageTemplateService.addSlot(id, command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Slot added successfully", mapSlotToResponse(slot)));
  }

  @DeleteMapping("/{id}/slots/{slotName}")
  public ResponseEntity<ApiResponse<Void>> removeSlot(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotBlank String slotName) {
    pageTemplateService.removeSlot(id, slotName);
    return ResponseEntity.ok(ApiResponse.success("Slot removed successfully", null));
  }

  @PostMapping("/{id}/assign/{pageId}")
  public ResponseEntity<ApiResponse<Void>> assignTemplateToPage(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull @Min(1) Long pageId) {
    pageTemplateService.assignTemplateToPage(pageId, id);
    return ResponseEntity.ok(ApiResponse.success("Template assigned to page successfully", null));
  }

  @PutMapping("/{id}/slots/reorder")
  public ResponseEntity<ApiResponse<Void>> reorderSlots(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody ReorderRequest<String> request) {
    com.backend.application.command.PageTemplateCommands.ReorderTemplateSlotsCommand command = new com.backend.application.command.PageTemplateCommands.ReorderTemplateSlotsCommand(
        request.items());

    pageTemplateService.reorderSlots(id, command);
    return ResponseEntity.ok(ApiResponse.success("Slots reordered successfully", null));
  }

  @GetMapping("/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageTemplateI18nResponse>> getTemplateI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language) {
    PageTemplateI18nResponse response = pageTemplateI18nService.getTemplateI18n(id, language);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageTemplateI18nResponse>> upsertTemplateI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody PageTemplateI18nRequest request) {
    PageTemplateI18nResponse response = pageTemplateI18nService.upsertTemplateI18n(id, language, request);
    return ResponseEntity.ok(ApiResponse.success("Template i18n updated successfully", response));
  }

  private List<PageTemplateResponse> mapToResponses(List<PageTemplateDto> dtos) {
    return dtos.stream().map(this::mapToResponse).toList();
  }

  private PageTemplateResponse mapToResponse(PageTemplateDto dto) {
    return PageTemplateResponse.builder()
        .id(dto.getId())
        .uuid(dto.getUuid())
        .uid(dto.getUid())
        .name(dto.getName())
        .description(dto.getDescription())
        .isSystem(dto.getIsSystem())
        .isActive(dto.getIsActive())
        .slots(dto.getSlots() != null
            ? dto.getSlots().stream().map(this::mapSlotToResponse).toList()
            : List.of())
        .build();
  }

  private TemplateSlotResponse mapSlotToResponse(TemplateSlotDto dto) {
    return TemplateSlotResponse.builder()
        .id(dto.getId())
        .uuid(dto.getUuid())
        .slotName(dto.getSlotName())
        .position(dto.getPosition())
        .sortOrder(dto.getSortOrder())
        .isRequired(dto.getIsRequired())
        .maxComponents(dto.getMaxComponents())
        .allowedTypes(dto.getAllowedTypes())
        .build();
  }
}
