package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.command.PageTemplateCommands.CreatePageTemplateCommand;
import com.backend.application.command.PageTemplateCommands.CreateTemplateSlotCommand;
import com.backend.application.command.PageTemplateCommands.UpdatePageTemplateCommand;
import com.backend.application.dto.response.BulkDeleteResultResponse;
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
import com.backend.presentation.dto.request.BulkDeleteRequest;
import com.backend.presentation.dto.response.PageTemplateResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.TemplateSlotResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/page-templates")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
public class PageTemplateController {

  private final PageTemplateService pageTemplateService;
  private final PageTemplateI18nService pageTemplateI18nService;
  private final MessageSource messageSource;

  @GetMapping
  public ResponseEntity<ApiResponse<PageableResponse<PageTemplateResponse>>> getAllTemplates(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String search,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
          SortableFieldsConfig.PAGE_TEMPLATE_DEFAULT_SORT);
      Sort sortObj = SortParseUtil.parse(effectiveSort, SortableFieldsConfig.PAGE_TEMPLATE_ALLOWED_FIELDS,
          SortableFieldsConfig.PAGE_TEMPLATE_DEFAULT_SORT);

      PageRequest pageRequest = PageRequest.of(page, size, sortObj);
      Page<PageTemplateDto> templatesPage = pageTemplateService.getTemplates(pageRequest, search);

      List<PageTemplateResponse> content = templatesPage.getContent().stream()
          .map(this::mapToResponse)
          .toList();

      SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.PAGE_TEMPLATE_SORT_OPTIONS);
      PageableResponse<PageTemplateResponse> response = PageableResponse.fromMapped(templatesPage, content, sortConfig);

      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (IllegalArgumentException ex) {
      String message = messageSource.getMessage("pageTemplate.sort.invalid",
          new Object[] { ex.getMessage() },
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      String message = messageSource.getMessage("pageTemplate.list.error",
          new Object[] { ex.getMessage() },
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteTemplate(
      @PathVariable @NotNull @Min(1) Long id) {
    pageTemplateService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/bulk-delete")
  public ResponseEntity<ApiResponse<BulkDeleteResultResponse>> bulkDeleteTemplates(
      @Valid @RequestBody BulkDeleteRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      BulkDeleteResultResponse result = pageTemplateService.bulkDeletePageTemplates(request.ids());
      int requested = request.ids().size();
      if (result.deletedIds().isEmpty() && result.failedIds().size() == requested && requested > 0) {
        String allFailedMsg = messageSource.getMessage("pageTemplate.bulk.delete.allFailed", null,
            Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.success(allFailedMsg, result));
      }
      String successMessage = messageSource.getMessage("pageTemplate.bulk.delete.success",
          new Object[] { result.deletedIds().size(), result.failedIds().size() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, result));
    } catch (Exception ex) {
      log.error("Error bulk deleting templates: {}", ex.getMessage(), ex);
      String msg = messageSource.getMessage("pageTemplate.bulk.delete.error", null,
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/{id}/slots/{slotName}")
  public ResponseEntity<ApiResponse<Void>> removeSlot(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotBlank String slotName) {
    pageTemplateService.removeSlot(id, slotName);
    return ResponseEntity.ok(ApiResponse.success("Slot removed successfully", null));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/{id}/assign/{pageId}")
  public ResponseEntity<ApiResponse<Void>> assignTemplateToPage(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull @Min(1) Long pageId) {
    pageTemplateService.assignTemplateToPage(pageId, id);
    return ResponseEntity.ok(ApiResponse.success("Template assigned to page successfully", null));
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
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
