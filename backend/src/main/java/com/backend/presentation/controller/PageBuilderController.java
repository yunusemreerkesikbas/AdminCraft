package com.backend.presentation.controller;

import com.backend.application.service.PageBuilderService;
import com.backend.domain.entity.PageBlock;
import com.backend.domain.entity.PageSection;
import com.backend.presentation.dto.request.CreateBlockRequest;
import com.backend.presentation.dto.request.CreateSectionRequest;
import com.backend.presentation.dto.request.UpdateBlockRequest;
import com.backend.presentation.dto.request.UpdateSectionRequest;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/page-builder")
@RequiredArgsConstructor
@Validated
public class PageBuilderController {

  private final PageBuilderService builderService;
  private final MessageSource messageSource;

  // Sections
  @PostMapping("/sections")
  public ResponseEntity<ApiResponse<PageSection>> addSection(
      @Valid @RequestBody CreateSectionRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageSection s = builderService.addSection(req.pageId(), req.type(), req.displayOrder(), req.data());
      return ResponseEntity.ok(ApiResponse.success(s));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.section.create.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/sections")
  public ResponseEntity<ApiResponse<PageSection>> updateSection(
      @Valid @RequestBody UpdateSectionRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageSection s = builderService.updateSection(req.id(), req.type(), req.displayOrder(), req.data());
      return ResponseEntity.ok(ApiResponse.success(s));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.section.update.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/sections")
  public ResponseEntity<ApiResponse<List<PageSection>>> listSections(
      @RequestParam @NotNull Long pageId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      return ResponseEntity.ok(ApiResponse.success(builderService.listSections(pageId)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.section.list.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @DeleteMapping("/sections/{id}")
  public ResponseEntity<ApiResponse<Void>> removeSection(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      builderService.removeSection(id);
      return ResponseEntity.ok(ApiResponse.success("Section deleted", null));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.section.delete.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  // Blocks
  @PostMapping("/blocks")
  public ResponseEntity<ApiResponse<PageBlock>> addBlock(
      @Valid @RequestBody CreateBlockRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageBlock b = builderService.addBlock(req.sectionId(), req.type(), req.displayOrder(), req.data());
      return ResponseEntity.ok(ApiResponse.success(b));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.block.create.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/blocks")
  public ResponseEntity<ApiResponse<PageBlock>> updateBlock(
      @Valid @RequestBody UpdateBlockRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageBlock b = builderService.updateBlock(req.id(), req.type(), req.displayOrder(), req.data());
      return ResponseEntity.ok(ApiResponse.success(b));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.block.update.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/blocks")
  public ResponseEntity<ApiResponse<List<PageBlock>>> listBlocks(
      @RequestParam @NotNull Long sectionId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      return ResponseEntity.ok(ApiResponse.success(builderService.listBlocks(sectionId)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.block.list.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @DeleteMapping("/blocks/{id}")
  public ResponseEntity<ApiResponse<Void>> removeBlock(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      builderService.removeBlock(id);
      return ResponseEntity.ok(ApiResponse.success("Block deleted", null));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.block.delete.error", new Object[] { ex.getMessage() },
          Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }
}
