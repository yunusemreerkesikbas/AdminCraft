package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
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

import com.backend.application.dto.request.CreatePageCompositeRequest;
import com.backend.application.dto.request.PageCreateRequest;
import com.backend.application.dto.request.PageI18nRequest;
import com.backend.application.dto.request.PagePublishRequest;
import com.backend.application.dto.request.UpdatePageCompositeRequest;
import com.backend.application.service.PageI18nService;
import com.backend.application.service.PageService;
import com.backend.domain.enums.Language;
import com.backend.application.dto.response.PageDetailResponse;
import com.backend.application.dto.response.PageI18nResponse;
import com.backend.application.dto.response.PageListResponse;
import com.backend.application.dto.response.PageResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Pages", description = "Page CRUD and translation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PageController {

  private final PageService pageService;
  private final PageI18nService pageI18nService;
  private final MessageSource messageSource;

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping
  @Operation(summary = "Create page", description = "Creates a new page")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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
  @Operation(summary = "Get page by ID", description = "Returns page by ID, optionally with translations")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Page not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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
  @Operation(summary = "List pages", description = "Returns all pages with translation metadata")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pages returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/{id}")
  @Operation(summary = "Update page", description = "Updates an existing page")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete page", description = "Deletes a page by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page deleted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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
  @Operation(summary = "Get page translation", description = "Returns translation details for a page and language")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
  })
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/{pageId}/i18n/{language}")
  @Operation(summary = "Upsert page translation", description = "Creates or updates page translation for selected language")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation upserted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/{pageId}/publish/{language}")
  @Operation(summary = "Publish page translation", description = "Publishes page translation for selected language")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation published"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
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

  // ==================== Composite Endpoints (Sprint 34 Pattern)
  // ====================

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/composite")
  @Operation(summary = "Create page composite", description = "Creates page with translation payload in a composite request")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Composite created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<PageDetailResponse>> createComposite(
      @Valid @RequestBody CreatePageCompositeRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Long userId = SecurityUtil.getCurrentUserIdOrThrow();
      PageDetailResponse response = pageService.createComposite(request, userId);
      String successMessage = messageSource.getMessage("page.create.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(successMessage, response));
    } catch (Exception ex) {
      log.error("Error creating page composite", ex);
      // OWASP: Do not expose exception details to client
      String msg = messageSource.getMessage("page.create.error", null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}/composite")
  @Operation(summary = "Get page composite", description = "Returns page with all translation details")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Page not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<PageDetailResponse>> getComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      PageDetailResponse response = pageService.getPageWithI18n(id);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting page composite id={}", id, ex);
      String msg = messageSource.getMessage("page.get.error", null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(msg));
    }
  }

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/{id}/composite")
  @Operation(summary = "Update page composite", description = "Updates page and translation payload in composite mode")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Composite updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ApiResponse<PageDetailResponse>> updateComposite(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdatePageCompositeRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Long userId = SecurityUtil.getCurrentUserIdOrThrow();
      PageDetailResponse response = pageService.updateComposite(id, request, userId);
      String successMessage = messageSource.getMessage("page.update.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    } catch (Exception ex) {
      log.error("Error updating page composite id={}", id, ex);
      // OWASP: Do not expose exception details to client
      String msg = messageSource.getMessage("page.update.error", null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }
}
