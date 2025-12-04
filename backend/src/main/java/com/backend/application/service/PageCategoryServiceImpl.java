package com.backend.application.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.CreatePageCategoryCommand;
import com.backend.application.command.UpdatePageCategoryCommand;
import com.backend.application.command.UpsertPageCategoryI18nCommand;
import com.backend.application.query.PageCategoryDetailQuery;
import com.backend.domain.entity.PageCategory;
import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.CategoryNotFoundException;
import com.backend.domain.repository.PageCategoryI18nRepository;
import com.backend.domain.repository.PageCategoryRepository;
import com.backend.presentation.dto.response.PageCategoryDetailResponse;
import com.backend.presentation.dto.response.PageCategoryI18nResponse;
import com.backend.presentation.dto.response.PageCategoryListResponse;
import com.backend.presentation.dto.response.PageCategoryMetadataResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PageCategoryServiceImpl implements PageCategoryService {

  private final PageCategoryRepository categoryRepository;
  private final PageCategoryI18nRepository i18nRepository;

  @Override
  public PageCategoryDetailResponse create(CreatePageCategoryCommand command, Long tenantId) {
    if (command.uid() != null && !command.uid().trim().isEmpty()) {
      if (categoryRepository.existsByTenantIdAndUid(tenantId, command.uid())) {
        throw new IllegalArgumentException("UID already exists for this tenant");
      }
    }

    if (command.parentId() != null) {
      validateParentBelongsToTenant(command.parentId(), tenantId);
    }

    PageCategory category = new PageCategory();
    category.setUid(command.uid());
    category.setParentId(command.parentId());
    category.setActive(command.active() != null ? command.active() : true);
    category.setStyleClasses(command.styleClasses());
    category.setSortOrder(command.sortOrder() != null ? command.sortOrder() : 0);

    PageCategory saved = categoryRepository.save(category);

    return toDetailResponse(saved, Collections.emptyMap(), 0, 0);
  }

  @Override
  public PageCategoryDetailResponse update(Long id, UpdatePageCategoryCommand command, Long tenantId) {
    PageCategory existing = categoryRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    if (command.parentId() != null) {
      if (command.parentId().equals(id)) {
        throw new IllegalArgumentException("Category cannot be parent of itself");
      }
      validateParentBelongsToTenant(command.parentId(), tenantId);
    }

    if (command.parentId() != null) {
      existing.setParentId(command.parentId());
    }
    if (command.active() != null) {
      existing.setActive(command.active());
    }
    if (command.styleClasses() != null) {
      existing.setStyleClasses(command.styleClasses());
    }
    if (command.sortOrder() != null) {
      existing.setSortOrder(command.sortOrder());
    }

    PageCategory updated = categoryRepository.save(existing);

    return toDetailResponse(updated, Collections.emptyMap(), 0, 0);
  }

  @Override
  public void delete(Long id, Long tenantId) {
    PageCategory category = categoryRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    categoryRepository.delete(category);
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

    if (categories.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> categoryIds = categories.stream()
        .map(PageCategory::getId)
        .toList();

    List<PageCategoryI18n> allTranslations = i18nRepository
        .findByCategoryIdIn(categoryIds);

    Map<Long, List<PageCategoryI18n>> translationsByCategory = allTranslations.stream()
        .collect(Collectors.groupingBy(i18n -> i18n.getCategory().getId()));

    return categories.stream()
        .map(cat -> toListResponse(cat,
            translationsByCategory.getOrDefault(cat.getId(), Collections.emptyList())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageCategoryDetailResponse getDetailById(PageCategoryDetailQuery query) {
    PageCategory category = categoryRepository.findByIdAndTenantId(query.id(), query.tenantId())
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    Map<String, PageCategoryI18nResponse> translationsMap = new HashMap<>();
    int translationCount = 0;
    int publishedCount = 0;

    if (query.includeTranslations()) {
      List<PageCategoryI18n> translations = i18nRepository.findByCategoryId(query.id());
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
      UpsertPageCategoryI18nCommand command, Long tenantId) {

    PageCategory category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    if (command.url() != null && !command.url().isEmpty()) {
      boolean urlExists = i18nRepository.existsByLanguageAndUrlAndCategoryIdNot(
          language, command.url(), categoryId);
      if (urlExists) {
        throw new IllegalArgumentException("URL already exists for this language");
      }
    }

    Optional<PageCategoryI18n> existing = i18nRepository.findByCategoryIdAndLanguage(
        categoryId, language);

    PageCategoryI18n i18n;
    if (existing.isPresent()) {
      i18n = existing.get();
    } else {
      i18n = new PageCategoryI18n();
      i18n.setCategory(category);
      i18n.setLanguage(language);
    }

    if (command.url() != null) {
      i18n.setUrl(command.url());
    }
    if (command.title() != null) {
      i18n.setTitle(command.title());
    }
    if (command.metaTitle() != null) {
      i18n.setMetaTitle(command.metaTitle());
    }
    if (command.metaDescription() != null) {
      i18n.setMetaDescription(command.metaDescription());
    }
    if (command.active() != null) {
      i18n.setActive(command.active());
    }

    PageCategoryI18n saved = i18nRepository.save(i18n);
    return toI18nResponse(saved, false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategoryI18n> getI18n(Long categoryId, Language language, Long tenantId) {
    return i18nRepository.findByCategoryIdAndLanguage(categoryId, language);
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
