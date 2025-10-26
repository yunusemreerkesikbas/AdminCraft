package com.backend.presentation.controller;

import com.backend.application.service.PageI18nService;
import com.backend.application.service.PageService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.request.PageI18nRequest;
import com.backend.presentation.dto.request.PagePublishRequest;
import com.backend.presentation.dto.response.PageI18nResponse;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.presentation.dto.response.PageListResponse;
import com.backend.presentation.dto.response.PageDetailResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
public class PageController {

  private final PageService pageService;
  private final PageI18nService pageI18nService;
  private final MessageSource messageSource;

  @PostMapping
  public ResponseEntity<ApiResponse<PageResponse>> create(
      @Valid @RequestBody PageCreateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Long userId = SecurityUtil.getCurrentUserIdOrThrow();
      PageResponse response = pageService.createPage(request, userId);
      String successMessage = messageSource.getMessage("page.create.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (Exception ex) {
      log.error("Error creating page: {}", ex.getMessage());
      String msg = messageSource.getMessage("page.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<?>> getById(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestParam(value = "include", required = false) String include,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      if (include != null && include.contains("translations")) {
        PageDetailResponse withI18n = pageService.getPageWithI18n(id);
        return ResponseEntity.ok(ApiResponse.success(withI18n));
      }
      PageResponse response = pageService.getPageById(id);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting page {}: {}", id, ex.getMessage());
      String msg = messageSource.getMessage("page.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageListResponse>>> list(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      List<PageListResponse> pages = pageService.getAllPagesWithTranslations();
      return ResponseEntity.ok(ApiResponse.success(pages));
    } catch (Exception ex) {
      log.error("Error listing pages: {}", ex.getMessage());
      String msg = messageSource.getMessage("page.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PageResponse>> update(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody PageCreateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Long userId = SecurityUtil.getCurrentUserIdOrThrow();
      PageResponse response = pageService.updatePage(id, request, userId);
      String successMessage = messageSource.getMessage("page.update.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (Exception ex) {
      log.error("Error updating page {}: {}", id, ex.getMessage());
      String msg = messageSource.getMessage("page.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      pageService.deletePage(id);
      String msg = messageSource.getMessage("page.delete.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));
    } catch (Exception ex) {
      log.error("Error deleting page {}: {}", id, ex.getMessage());
      String msg = messageSource.getMessage("page.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{pageId}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageI18nResponse>> getPageI18n(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotNull Language language,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageI18nResponse response = pageI18nService.getPageI18n(pageId, language);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting page i18n for pageId={}, language={}: {}", pageId, language, ex.getMessage());
      String message = messageSource.getMessage("page.i18n.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  @PutMapping("/{pageId}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageI18nResponse>> upsertPageI18n(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody PageI18nRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageI18nResponse response = pageI18nService.upsertPageI18n(pageId, language, request);
      String successMessage = messageSource.getMessage("page.i18n.update.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error upserting page i18n for pageId={}, language={}: {}",
          pageId, language, ex.getMessage());
      String message = messageSource.getMessage("page.i18n.validation.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error upserting page i18n for pageId={}, language={}: {}",
          pageId, language, ex.getMessage());
      String message = messageSource.getMessage("page.i18n.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  @PostMapping("/{pageId}/publish/{language}")
  public ResponseEntity<ApiResponse<PageI18nResponse>> publishPageI18n(
      @PathVariable @NotNull @Min(1) Long pageId,
      @PathVariable @NotNull Language language,
      @Valid @RequestBody PagePublishRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageI18nResponse response = pageI18nService.publishPageI18n(pageId, language, request);
      String successMessage = messageSource.getMessage("page.i18n.publish.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (IllegalArgumentException ex) {
      log.warn("Validation error publishing page i18n for pageId={}, language={}: {}",
          pageId, language, ex.getMessage());
      String message = messageSource.getMessage("page.i18n.publish.validation.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error publishing page i18n for pageId={}, language={}: {}",
          pageId, language, ex.getMessage());
      String message = messageSource.getMessage("page.i18n.publish.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }
}
