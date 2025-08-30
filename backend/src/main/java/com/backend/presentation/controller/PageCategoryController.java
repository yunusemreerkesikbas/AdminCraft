package com.backend.presentation.controller;

import com.backend.application.service.PageCategoryService;
import com.backend.domain.entity.PageCategory;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/page-categories")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PageCategoryController {

  private final PageCategoryService categoryService;
  private final MessageSource messageSource;

  @GetMapping("/tree")
  public ResponseEntity<ApiResponse<List<com.backend.presentation.dto.response.PageCategoryDto>>> tree(
      @RequestParam @NotNull Long tenantId,
      @RequestParam(required = false) Long rootId,
      @RequestParam(required = false) Integer depth,
      @RequestParam(required = false, defaultValue = "tr") String lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String headerLang) {
    try {
      var list = categoryService.getTree(tenantId, lang, rootId, depth);
      return ResponseEntity.ok(ApiResponse.success(list));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.tree.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(headerLang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PageCategory>> create(
      @Valid @RequestBody com.backend.presentation.dto.request.CreatePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageCategory category = new PageCategory();
      category.setTenantId(req.tenantId());
      category.setName(req.name());
      category.setSlug(req.slug());
      category.setParentId(req.parentId());
      category.setSortOrder(req.sortOrder());
      PageCategory saved = categoryService.create(category);
      return ResponseEntity.ok(ApiResponse.success(saved));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @PutMapping
  public ResponseEntity<ApiResponse<PageCategory>> update(
      @Valid @RequestBody com.backend.presentation.dto.request.UpdatePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageCategory category = new PageCategory();
      category.setId(req.id());
      category.setTenantId(req.tenantId());
      category.setName(req.name());
      category.setSlug(req.slug());
      category.setParentId(req.parentId());
      category.setSortOrder(req.sortOrder());
      PageCategory updated = categoryService.update(category);
      return ResponseEntity.ok(ApiResponse.success(updated));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PageCategory>> getById(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Optional<PageCategory> c = categoryService.findById(id);
      if (c.isEmpty()) {
        String msg = messageSource.getMessage("page.category.not.found",
            new Object[] { id }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
      }
      return ResponseEntity.ok(ApiResponse.success(c.get()));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageCategory>>> list(
      @RequestParam @NotNull Long tenantId,
      @RequestParam(required = false) Long parentId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      List<PageCategory> list = parentId == null
          ? categoryService.listByTenant(tenantId)
          : categoryService.listChildren(tenantId, parentId);
      return ResponseEntity.ok(ApiResponse.success(list));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/children")
  public ResponseEntity<ApiResponse<List<com.backend.presentation.dto.response.PageCategoryDto>>> listChildren(
      @RequestParam @NotNull Long tenantId,
      @RequestParam(required = false) Long parentId,
      @RequestParam(required = false, defaultValue = "tr") String lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String headerLang) {
    try {
      var list = categoryService.listChildrenLocalized(tenantId, parentId, lang);
      return ResponseEntity.ok(ApiResponse.success(list));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.children.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(headerLang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}/move")
  public ResponseEntity<ApiResponse<Void>> move(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody com.backend.presentation.dto.request.MovePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      categoryService.move(req.tenantId(), id, req.newParentId());
      String msg = messageSource.getMessage("page.category.move.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.move.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/reorder")
  public ResponseEntity<ApiResponse<Void>> reorder(
      @Valid @RequestBody com.backend.presentation.dto.request.ReorderPageCategoriesRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      categoryService.reorder(req.tenantId(), req.parentId(), req.orderedIds());
      String msg = messageSource.getMessage("page.category.reorder.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.reorder.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      categoryService.delete(id);
      String msg = messageSource.getMessage("page.category.delete.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.category.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }
  }
}
