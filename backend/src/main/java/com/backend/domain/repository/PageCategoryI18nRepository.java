package com.backend.domain.repository;

import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Optional;

public interface PageCategoryI18nRepository {
  PageCategoryI18n save(PageCategoryI18n translation);

  List<PageCategoryI18n> saveAll(Iterable<PageCategoryI18n> translations);

  Optional<PageCategoryI18n> findByTenantIdAndCategoryIdAndLanguage(
      Long tenantId, Long categoryId, Language language);

  List<PageCategoryI18n> findByTenantIdAndCategoryIdInAndLanguage(
      Long tenantId, List<Long> categoryIds, Language language);

  List<PageCategoryI18n> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

  List<PageCategoryI18n> findByTenantIdAndCategoryIdIn(Long tenantId, List<Long> categoryIds);

  boolean existsByTenantIdAndLanguageAndUrl(Long tenantId, Language language, String url);

  boolean existsByTenantIdAndLanguageAndUrlAndCategoryIdNot(
      Long tenantId, Language language, String url, Long categoryId);
}
