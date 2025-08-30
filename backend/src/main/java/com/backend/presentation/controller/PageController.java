package com.backend.presentation.controller;

import com.backend.application.service.PageService;
import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreatePageRequest;
import com.backend.presentation.dto.request.UpdatePageRequest;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.exception.PageCannotBePublishedException;
import com.backend.domain.exception.UnauthorizedOperationException;
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
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PageController {

  private final PageService pageService;
  private final MessageSource messageSource;

  @PostMapping
  public ResponseEntity<ApiResponse<PageResponse>> create(
      @Valid @RequestBody CreatePageRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Page page = new Page();
      page.setTenantId(req.tenantId());
      page.setTitle(req.title());
      page.setSlug(req.slug());
      page.setLanguage(req.language());
      page.setCategoryId(req.categoryId());
      page.setMetaTitle(req.metaTitle());
      page.setMetaDescription(req.metaDescription());
      page.setCanonicalUrl(req.canonicalUrl());
      page.setCreatedBy(SecurityUtil.getCurrentUserIdOrThrow());

      Page saved = pageService.create(page);
      return ResponseEntity.ok(ApiResponse.success(toResponse(saved)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/slug/{language}/{slug}")
  public ResponseEntity<ApiResponse<PageResponse>> getBySlug(
      @RequestParam @NotNull Long tenantId,
      @PathVariable Language language,
      @PathVariable @NotBlank String slug,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Optional<Page> p = pageService.findBySlug(tenantId, slug, language);
      if (p.isEmpty()) {
        String msg = messageSource.getMessage("page.not.found",
            new Object[] { slug }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(msg));
      }
      return ResponseEntity.ok(ApiResponse.success(toResponse(p.get())));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(msg));
    }
  }

  @PutMapping
  public ResponseEntity<ApiResponse<PageResponse>> update(
      @Valid @RequestBody UpdatePageRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      // Load existing and apply changes to preserve immutable audit fields
      Page existing = pageService.findById(req.id())
          .orElseThrow(() -> new IllegalArgumentException("Page not found"));

      existing.setTenantId(req.tenantId());
      existing.setTitle(req.title());
      existing.setSlug(req.slug());
      existing.setLanguage(req.language());
      existing.setCategoryId(req.categoryId());
      existing.setMetaTitle(req.metaTitle());
      existing.setMetaDescription(req.metaDescription());
      existing.setCanonicalUrl(req.canonicalUrl());
      existing.setSubtitle(req.subtitle());
      existing.setStyleClasses(req.styleClasses());
      // HTML sanitize: Relaxed safelist + basic attributes
      String safeHtml = null;
      if (req.descriptionHtml() != null && !req.descriptionHtml().isBlank()) {
        safeHtml = Jsoup.clean(req.descriptionHtml(), Safelist.relaxed()
            .addAttributes(":all", "class", "style", "id")
            .addAttributes("a", "target", "rel"));
      } else if (req.description() != null && !req.description().isBlank()) {
        // Plain text -> escape + <p> wrap, then sanitize (no-op for none)
        String escaped = Jsoup.clean(req.description(), Safelist.none());
        safeHtml = "<p>" + escaped + "</p>";
      }
      // Derive plain text from sanitized HTML if needed
      String plainText = req.description();
      if ((plainText == null || plainText.isBlank()) && safeHtml != null) {
        plainText = Jsoup.parse(safeHtml).text();
      }
      existing.setDescription(plainText);
      existing.setDescriptionHtml(safeHtml);
      // descriptionFormat kaldırıldı (sade HTML/TEXT modeli)
      existing.setFeaturedImage(req.featuredImage());
      existing.setUpdatedBy(SecurityUtil.getCurrentUserIdOrThrow());

      Page updated = pageService.update(existing);
      return ResponseEntity.ok(ApiResponse.success(toResponse(updated)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PageResponse>> getById(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Optional<Page> page = pageService.findById(id);
      if (page.isEmpty()) {
        String msg = messageSource.getMessage("page.not.found",
            new Object[] { id }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(msg));
      }
      return ResponseEntity.ok(ApiResponse.success(toResponse(page.get())));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(msg));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageResponse>>> list(
      @RequestParam @NotNull Long tenantId,
      @RequestParam(required = false) Language language,
      @RequestParam(required = false) Long categoryId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      List<Page> pages;
      if (categoryId != null) {
        pages = pageService.listByCategory(tenantId, categoryId);
      } else if (language != null) {
        pages = pageService.listByTenantAndLanguage(tenantId, language);
      } else {
        pages = pageService.listByTenant(tenantId);
      }
      return ResponseEntity.ok(ApiResponse.success(pages.stream().map(this::toResponse).toList()));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}/publish")
  public ResponseEntity<ApiResponse<PageResponse>> publish(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Page published = pageService.publish(id, SecurityUtil.getCurrentUserIdOrThrow());
      return ResponseEntity.ok(ApiResponse.success(toResponse(published)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.publish.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}/unpublish")
  public ResponseEntity<ApiResponse<PageResponse>> unpublish(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Page p = pageService.unpublish(id, SecurityUtil.getCurrentUserIdOrThrow());
      return ResponseEntity.ok(ApiResponse.success(toResponse(p)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.unpublish.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  @PutMapping("/{id}/schedule")
  public ResponseEntity<ApiResponse<PageResponse>> schedule(
      @PathVariable @NotNull @Min(1) Long id,
      @RequestParam @NotNull LocalDateTime when,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      Page p = pageService.schedule(id, when, SecurityUtil.getCurrentUserIdOrThrow());
      return ResponseEntity.ok(ApiResponse.success(toResponse(p)));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.schedule.error",
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
      pageService.delete(id);
      String msg = messageSource.getMessage("page.delete.success",
          null, Locale.forLanguageTag(lang));
      return ResponseEntity.ok(ApiResponse.success(msg, null));
    } catch (PageNotFoundException ex) {
      String msg = messageSource.getMessage("page.not.found",
          new Object[] { id }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(msg));
    } catch (Exception ex) {
      String msg = messageSource.getMessage("page.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(msg));
    }
  }

  private PageResponse toResponse(Page p) {
    return new PageResponse(
        p.getId(),
        p.getTenantId(),
        p.getTitle(),
        p.getSlug(),
        p.getStatus(),
        p.getLanguage(),
        p.getCategoryId(),
        p.getMetaTitle(),
        p.getMetaDescription(),
        p.getCanonicalUrl(),
        p.getSubtitle(),
        p.getStyleClasses(),
        p.getDescription(),
        p.getDescriptionHtml(),
        p.getFeaturedImage(),
        p.getPublishedAt(),
        p.getScheduledAt(),
        p.getCreatedAt(),
        p.getUpdatedAt());
  }
}
