package com.backend.presentation.controller;

import com.backend.application.command.CreatePageCategoryCommand;
import com.backend.application.command.UpdatePageCategoryCommand;
import com.backend.application.command.UpsertPageCategoryI18nCommand;
import com.backend.application.query.PageCategoryDetailQuery;
import com.backend.application.service.PageCategoryService;
import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.CategoryNotFoundException;
import com.backend.presentation.dto.request.CreatePageCategoryRequest;
import com.backend.presentation.dto.request.UpdatePageCategoryRequest;
import com.backend.presentation.dto.request.UpsertPageCategoryI18nRequest;
import com.backend.presentation.dto.response.PageCategoryDetailResponse;
import com.backend.presentation.dto.response.PageCategoryI18nResponse;
import com.backend.presentation.dto.response.PageCategoryListResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/page-categories")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
public class PageCategoryController {

  private final PageCategoryService categoryService;
  private final MessageSource messageSource;
  private final SecurityHelper securityHelper;

  @PostMapping
  public ResponseEntity<ApiResponse<PageCategoryDetailResponse>> create(
      @Valid @RequestBody CreatePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long userTenantId = securityHelper.getCurrentUserTenantId();

      CreatePageCategoryCommand command = new CreatePageCategoryCommand(
          req.uid(),
          req.parentId(),
          req.active(),
          req.styleClasses(),
          req.sortOrder()
      );

      PageCategoryDetailResponse response = categoryService.create(command, userTenantId);

      log.info("SECURITY_AUDIT: Category created successfully - correlationId={}, categoryId={}, tenantId={}",
          correlationId, response.id(), userTenantId);

      String msg = messageSource.getMessage("page.category.create.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, response));

    } catch (CategoryNotFoundException ex) {
      log.warn("Business error in create endpoint [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (IllegalArgumentException ex) {
      log.warn("Validation error in create endpoint [{}]: {}", correlationId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));

    } catch (Exception ex) {
      log.error("Technical error in create endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.create.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PageCategoryDetailResponse>> update(
      @PathVariable @NotNull @Min(1) Long id,
      @Valid @RequestBody UpdatePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long userTenantId = securityHelper.getCurrentUserTenantId();

      UpdatePageCategoryCommand command = new UpdatePageCategoryCommand(
          req.parentId(),
          req.active(),
          req.styleClasses(),
          req.sortOrder()
      );

      PageCategoryDetailResponse response = categoryService.update(id, command, userTenantId);

      log.info("SECURITY_AUDIT: Category updated successfully - correlationId={}, categoryId={}, tenantId={}",
          correlationId, id, userTenantId);

      String msg = messageSource.getMessage("page.category.update.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, response));

    } catch (CategoryNotFoundException ex) {
      log.warn("Business error in update endpoint [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (IllegalArgumentException ex) {
      log.warn("Validation error in update endpoint [{}]: {}", correlationId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));

    } catch (Exception ex) {
      log.error("Technical error in update endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.update.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PageCategoryDetailResponse>> getById(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestParam(value = "include", required = false) String include,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long userTenantId = securityHelper.getCurrentUserTenantId();
      boolean includeTranslations = "translations".equals(include);

      PageCategoryDetailQuery query = new PageCategoryDetailQuery(id, userTenantId, includeTranslations);

      PageCategoryDetailResponse response = categoryService.getDetailById(query);

      log.debug("SECURITY_AUDIT: Category retrieved successfully - correlationId={}, categoryId={}, tenantId={}",
          correlationId, id, userTenantId);

      String msg = messageSource.getMessage("page.category.get.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, response));

    } catch (CategoryNotFoundException ex) {
      log.warn("Category not found [{}] - categoryId={}", correlationId, id);
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (Exception ex) {
      log.error("Technical error in getById endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.get.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageCategoryListResponse>>> list(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();

      List<PageCategoryListResponse> list = categoryService.listByTenant(tenantId);

      log.debug("SECURITY_AUDIT: Category list retrieved - correlationId={}, tenantId={}, count={}",
          correlationId, tenantId, list.size());

      String msg = messageSource.getMessage("page.category.list.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, list));

    } catch (Exception ex) {
      log.error("Technical error in list endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.list.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();
      categoryService.delete(id, tenantId);

      log.info("SECURITY_AUDIT: Category deleted successfully - correlationId={}, categoryId={}, tenantId={}",
          correlationId, id, tenantId);

      String msg = messageSource.getMessage("page.category.delete.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));

    } catch (CategoryNotFoundException ex) {
      log.warn("Category not found for deletion [{}] - categoryId={}", correlationId, id);
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (Exception ex) {
      log.error("Technical error in delete endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.delete.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageCategoryI18nResponse>> upsertI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull String language,
      @Valid @RequestBody UpsertPageCategoryI18nRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();
      Language languageEnum = Language.valueOf(language.toUpperCase());

      UpsertPageCategoryI18nCommand command = new UpsertPageCategoryI18nCommand(
          req.url(),
          req.title(),
          req.metaTitle(),
          req.metaDescription(),
          req.active()
      );

      PageCategoryI18nResponse response = categoryService.upsertI18n(id, languageEnum, command, tenantId);

      log.info("SECURITY_AUDIT: Category i18n upserted - correlationId={}, categoryId={}, language={}, tenantId={}",
          correlationId, id, language, tenantId);

      String msg = messageSource.getMessage("page.category.i18n.upsert.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, response));

    } catch (CategoryNotFoundException ex) {
      log.warn("Category not found for i18n [{}] - categoryId={}", correlationId, id);
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (IllegalArgumentException ex) {
      log.warn("Validation error in upsertI18n endpoint [{}]: {}", correlationId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));

    } catch (Exception ex) {
      log.error("Technical error in upsertI18n endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.i18n.upsert.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}/i18n/{language}")
  public ResponseEntity<ApiResponse<PageCategoryI18nResponse>> getI18n(
      @PathVariable @NotNull @Min(1) Long id,
      @PathVariable @NotNull String language,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

    String correlationId = UUID.randomUUID().toString();

    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();
      Language languageEnum = Language.valueOf(language.toUpperCase());

      Optional<PageCategoryI18n> i18nOpt = categoryService.getI18n(id, languageEnum, tenantId);

      if (i18nOpt.isEmpty()) {
        log.warn("Category i18n not found [{}] - categoryId={}, language={}", correlationId, id, language);
        String msg = messageSource.getMessage("page.category.i18n.not.found",
            new Object[] { id, language }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
      }

      PageCategoryI18n i18n = i18nOpt.get();
      PageCategoryI18nResponse response = new PageCategoryI18nResponse(
          i18n.getId(),
          i18n.getUuid(),
          i18n.getUid(),
          i18n.getLanguage(),
          i18n.getUrl(),
          i18n.getTitle(),
          i18n.getMetaTitle(),
          i18n.getMetaDescription(),
          i18n.getActive(),
          i18n.getUpdatedAt(),
          false);

      log.debug("SECURITY_AUDIT: Category i18n retrieved - correlationId={}, categoryId={}, language={}, tenantId={}",
          correlationId, id, language, tenantId);

      String msg = messageSource.getMessage("page.category.i18n.get.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, response));

    } catch (CategoryNotFoundException ex) {
      log.warn("Category not found for i18n [{}] - categoryId={}", correlationId, id);
      String msg = messageSource.getMessage("page.category.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));

    } catch (IllegalArgumentException ex) {
      log.warn("Invalid language [{}]: {}", correlationId, language);
      String msg = messageSource.getMessage("common.invalid.language",
          new Object[] { language }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));

    } catch (Exception ex) {
      log.error("Technical error in getI18n endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.i18n.get.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }
}
