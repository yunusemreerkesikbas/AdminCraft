package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.CategoryNotFoundException;
import com.backend.domain.repository.PageCategoryI18nRepository;
import com.backend.domain.repository.PageCategoryRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.CreatePageCategoryRequest;
import com.backend.presentation.dto.request.UpdatePageCategoryRequest;
import com.backend.presentation.dto.request.UpsertPageCategoryI18nRequest;
import com.backend.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PageCategoryServiceImpl implements PageCategoryService {

  private final PageCategoryRepository categoryRepository;
  private final PageCategoryI18nRepository i18nRepository;
  private final TenantRepository tenantRepository;

  @Override
  public PageCategoryDetailResponse create(CreatePageCategoryRequest request, Long tenantId) {
    if (request.parentId() != null) {
      validateParentBelongsToTenant(request.parentId(), tenantId);
    }

    PageCategory category = new PageCategory();
    category.setTenantId(tenantId);
    category.setUid(request.uid());
    category.setParentId(request.parentId());
    category.setActive(request.active() != null ? request.active() : true);
    category.setStyleClasses(request.styleClasses());
    category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);

    PageCategory saved = categoryRepository.save(category);

    return toDetailResponse(saved, Collections.emptyMap(), 0, 0);
  }

  @Override
  public PageCategoryDetailResponse update(Long id, UpdatePageCategoryRequest request, Long tenantId) {
    PageCategory existing = categoryRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    if (request.parentId() != null) {
      if (request.parentId().equals(id)) {
        throw new IllegalArgumentException("Category cannot be parent of itself");
      }
      validateParentBelongsToTenant(request.parentId(), tenantId);
    }

    if (request.parentId() != null) {
      existing.setParentId(request.parentId());
    }
    if (request.active() != null) {
      existing.setActive(request.active());
    }
    if (request.styleClasses() != null) {
      existing.setStyleClasses(request.styleClasses());
    }
    if (request.sortOrder() != null) {
      existing.setSortOrder(request.sortOrder());
    }

    PageCategory updated = categoryRepository.save(existing);

    return toDetailResponse(updated, Collections.emptyMap(), 0, 0);
  }

  @Override
  public void delete(Long id, Long tenantId) {
    PageCategory category = categoryRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    categoryRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategory> findById(Long id, Long tenantId) {
    return categoryRepository.findByIdAndTenantId(id, tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategoryListResponse> listByTenant(Long tenantId) {
    List<PageCategory> categories = categoryRepository.findByTenantIdOrderBySortOrderAsc(tenantId);

    List<Long> categoryIds = categories.stream()
        .map(PageCategory::getId)
        .toList();

    List<PageCategoryI18n> allTranslations = categoryIds.isEmpty()
        ? Collections.emptyList()
        : i18nRepository.findByTenantIdAndCategoryId(tenantId, categoryIds.get(0));

    Map<Long, List<PageCategoryI18n>> translationsByCategory = new HashMap<>();
    for (Long catId : categoryIds) {
      translationsByCategory.put(catId,
          i18nRepository.findByTenantIdAndCategoryId(tenantId, catId));
    }

    return categories.stream()
        .map(cat -> toListResponse(cat, translationsByCategory.getOrDefault(cat.getId(), Collections.emptyList())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageCategoryDetailResponse getDetailById(Long id, Long tenantId, boolean includeTranslations) {
    PageCategory category = categoryRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    Map<String, PageCategoryI18nResponse> translationsMap = new HashMap<>();
    int translationCount = 0;
    int publishedCount = 0;

    if (includeTranslations) {
      List<PageCategoryI18n> translations = i18nRepository.findByTenantIdAndCategoryId(tenantId, id);
      translationCount = translations.size();

      for (PageCategoryI18n i18n : translations) {
        translationsMap.put(i18n.getLanguage().name(), toI18nResponse(i18n, false));
        if (i18n.getActive()) {
          publishedCount++;
        }
      }
    }

    return toDetailResponse(category, translationsMap, translationCount, publishedCount);
  }

  @Override
  public PageCategoryI18nResponse upsertI18n(Long categoryId, Language language,
      UpsertPageCategoryI18nRequest request, Long tenantId) {

    PageCategory category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    if (request.url() != null && !request.url().isEmpty()) {
      boolean urlExists = i18nRepository.existsByTenantIdAndLanguageAndUrlAndCategoryIdNot(
          tenantId, language, request.url(), categoryId);
      if (urlExists) {
        throw new IllegalArgumentException("URL already exists for this language");
      }
    }

    Optional<PageCategoryI18n> existing = i18nRepository.findByTenantIdAndCategoryIdAndLanguage(
        tenantId, categoryId, language);

    PageCategoryI18n i18n;
    if (existing.isPresent()) {
      i18n = existing.get();
    } else {
      i18n = new PageCategoryI18n();
      i18n.setTenantId(tenantId);
      i18n.setCategoryId(categoryId);
      i18n.setLanguage(language);
    }

    if (request.url() != null) {
      i18n.setUrl(request.url());
    }
    if (request.title() != null) {
      i18n.setTitle(request.title());
    }
    if (request.metaTitle() != null) {
      i18n.setMetaTitle(request.metaTitle());
    }
    if (request.metaDescription() != null) {
      i18n.setMetaDescription(request.metaDescription());
    }
    if (request.active() != null) {
      i18n.setActive(request.active());
    }

    PageCategoryI18n saved = i18nRepository.save(i18n);
    return toI18nResponse(saved, false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategoryI18n> getI18n(Long categoryId, Language language, Long tenantId) {
    return i18nRepository.findByTenantIdAndCategoryIdAndLanguage(tenantId, categoryId, language);
  }

  private void validateParentBelongsToTenant(Long parentId, Long tenantId) {
    if (parentId == null) {
      return;
    }

    Optional<PageCategory> parent = categoryRepository.findByIdAndTenantId(parentId, tenantId);
    if (parent.isEmpty()) {
      log.warn("SECURITY_ALERT: Attempt to use parent category {} from different tenant by tenant {}",
          parentId, tenantId);
      throw new IllegalArgumentException("Parent category does not belong to the specified tenant");
    }
  }

  private PageCategoryListResponse toListResponse(PageCategory category, List<PageCategoryI18n> translations) {
    Map<String, Boolean> translationsMap = new HashMap<>();

    for (Language lang : Language.values()) {
      boolean hasTranslation = translations.stream()
          .anyMatch(t -> t.getLanguage() == lang);
      translationsMap.put(lang.name(), hasTranslation);
    }

    return new PageCategoryListResponse(
        category.getId(),
        category.getUuid(),
        category.getUid(),
        category.getParentId(),
        category.getActive(),
        category.getStyleClasses(),
        category.getSortOrder(),
        category.getCreatedAt(),
        category.getUpdatedAt(),
        translationsMap);
  }

  private PageCategoryDetailResponse toDetailResponse(PageCategory category,
      Map<String, PageCategoryI18nResponse> translations, int translationCount, int publishedCount) {

    PageCategoryMetadataResponse metadata = new PageCategoryMetadataResponse(
        translationCount,
        publishedCount);

    return new PageCategoryDetailResponse(
        category.getId(),
        category.getUuid(),
        category.getUid(),
        category.getParentId(),
        category.getActive(),
        category.getStyleClasses(),
        category.getSortOrder(),
        category.getCreatedAt(),
        category.getUpdatedAt(),
        translations,
        metadata);
  }

  private PageCategoryI18nResponse toI18nResponse(PageCategoryI18n i18n, boolean isFallback) {
    return new PageCategoryI18nResponse(
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
        isFallback);
  }
}
