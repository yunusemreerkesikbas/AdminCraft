package com.backend.presentation.controller;

import com.backend.application.service.PageCategoryService;
import com.backend.domain.entity.PageCategory;
import com.backend.domain.exception.CategoryNotFoundException;
import com.backend.domain.exception.TenantMismatchException;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@PreAuthorize("hasRole('USER')") // Tüm endpoint'ler için authentication gerekli
public class PageCategoryController {

  private final PageCategoryService categoryService;
  private final MessageSource messageSource;
  private final SecurityHelper securityHelper;

  @GetMapping("/tree")
  public ResponseEntity<ApiResponse<List<com.backend.presentation.dto.response.PageCategoryDto>>> tree(
      @RequestParam(required = false) Long rootId,
      @RequestParam(required = false) Integer depth,
      @RequestParam(required = false, defaultValue = "tr") String lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String headerLang) {
    
    String correlationId = UUID.randomUUID().toString();
    log.debug("SECURITY_AUDIT: Tree request started - correlationId={}, user={}, rootId={}", 
              correlationId, securityHelper.getCurrentUserEmail(), rootId);
    
    try {
      // Güvenlik: Kullanıcının kendi tenant'ından tenantId al
      Long tenantId = securityHelper.getCurrentUserTenantId();
      
      var list = categoryService.getTree(tenantId, lang, rootId, depth);
      
      log.info("SECURITY_AUDIT: Tree request completed successfully - correlationId={}, tenantId={}, itemCount={}", 
               correlationId, tenantId, list.size());
      
      return ResponseEntity.ok(ApiResponse.success(list));
      
    } catch (CategoryNotFoundException ex) {
      log.warn("Business error in tree endpoint [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("page.category.not.found", 
          new Object[]{ex.getMessage()}, Locale.forLanguageTag(headerLang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
      
    } catch (TenantMismatchException ex) {
      log.warn("SECURITY_ALERT: Tenant access attempt [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("common.access.denied", null, Locale.forLanguageTag(headerLang));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg));
      
    } catch (Exception ex) {
      log.error("Technical error in tree endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.tree.technical.error", 
          new Object[]{correlationId}, Locale.forLanguageTag(headerLang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PageCategory>> create(
      @Valid @RequestBody com.backend.presentation.dto.request.CreatePageCategoryRequest req,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    
    String correlationId = UUID.randomUUID().toString();
    
    try {
      // Güvenlik: Kullanıcının kendi tenant'ını al
      Long userTenantId = securityHelper.getCurrentUserTenantId();
      
      // Güvenlik: Request'teki tenant ID'yi kullanıcının tenant'ı ile karşılaştır
      if (req.tenantId() != null && !req.tenantId().equals(userTenantId)) {
        log.warn("SECURITY_ALERT: Cross-tenant create attempt [{}] - user tenant: {}, requested tenant: {}", 
                 correlationId, userTenantId, req.tenantId());
        String msg = messageSource.getMessage("common.access.denied", null, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg));
      }
      
      // Güvenlik: Parent ID validasyonu (eğer varsa aynı tenant'tan olmalı)
      if (req.parentId() != null) {
        categoryService.validateParentBelongsToTenant(req.parentId(), userTenantId);
      }
      
      PageCategory category = new PageCategory();
      category.setTenantId(userTenantId); // Güvenlik: User'ın kendi tenant'ını kullan
      category.setName(req.name());
      category.setSlug(req.slug());
      category.setParentId(req.parentId());
      category.setSortOrder(req.sortOrder());
      
      PageCategory saved = categoryService.create(category);
      
      log.info("SECURITY_AUDIT: Category created successfully - correlationId={}, categoryId={}, tenantId={}", 
               correlationId, saved.getId(), userTenantId);
      
      return ResponseEntity.ok(ApiResponse.success(saved));
      
    } catch (CategoryNotFoundException ex) {
      log.warn("Business error in create endpoint [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("page.category.not.found", 
          new Object[]{ex.getMessage()}, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
      
    } catch (TenantMismatchException ex) {
      log.warn("SECURITY_ALERT: Tenant mismatch in create [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("common.access.denied", null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg));
      
    } catch (Exception ex) {
      log.error("Technical error in create endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.create.technical.error", 
          new Object[]{correlationId}, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
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
    
    String correlationId = UUID.randomUUID().toString();
    
    try {
      // Güvenlik: Kullanıcının kendi tenant'ını al
      Long userTenantId = securityHelper.getCurrentUserTenantId();
      
      // Güvenlik: Kategoriyi sadece kendi tenant'ından getir
      Optional<PageCategory> c = categoryService.findByIdAndTenantId(id, userTenantId);
      
      if (c.isEmpty()) {
        log.warn("Category not found or access denied [{}] - categoryId={}, tenantId={}", 
                 correlationId, id, userTenantId);
        String msg = messageSource.getMessage("page.category.not.found",
            new Object[] { id }, Locale.forLanguageTag(lang));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
      }
      
      log.debug("SECURITY_AUDIT: Category retrieved successfully - correlationId={}, categoryId={}, tenantId={}", 
                correlationId, id, userTenantId);
      
      return ResponseEntity.ok(ApiResponse.success(c.get()));
      
    } catch (TenantMismatchException ex) {
      log.warn("SECURITY_ALERT: Tenant access attempt in getById [{}]: {}", correlationId, ex.getMessage());
      String msg = messageSource.getMessage("common.access.denied", null, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg));
      
    } catch (Exception ex) {
      log.error("Technical error in getById endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.get.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PageCategory>>> list(
      @RequestParam(required = false) Long parentId,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    
    String correlationId = UUID.randomUUID().toString();
    
    try {
      // Güvenlik: Kullanıcının kendi tenant'ını al
      Long tenantId = securityHelper.getCurrentUserTenantId();
      
      List<PageCategory> list = parentId == null
          ? categoryService.listByTenant(tenantId)
          : categoryService.listChildren(tenantId, parentId);
          
      log.debug("SECURITY_AUDIT: Category list retrieved - correlationId={}, tenantId={}, parentId={}, count={}", 
                correlationId, tenantId, parentId, list.size());
      
      return ResponseEntity.ok(ApiResponse.success(list));
      
    } catch (Exception ex) {
      log.error("Technical error in list endpoint [{}]: {}", correlationId, ex.getMessage(), ex);
      String msg = messageSource.getMessage("page.category.list.technical.error",
          new Object[] { correlationId }, Locale.forLanguageTag(lang));
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
