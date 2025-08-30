package com.backend.domain.repository;

import com.backend.domain.entity.PageCategoryTranslation;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Optional;

public interface PageCategoryTranslationRepository {
  PageCategoryTranslation save(PageCategoryTranslation translation);

  List<PageCategoryTranslation> saveAll(Iterable<PageCategoryTranslation> translations);

  Optional<PageCategoryTranslation> findByTenantIdAndCategoryIdAndLanguage(
      Long tenantId, Long categoryId, Language language);

  List<PageCategoryTranslation> findByTenantIdAndCategoryIdInAndLanguage(
      Long tenantId, List<Long> categoryIds, Language language);

  List<PageCategoryTranslation> findByTenantIdAndCategoryIdIn(Long tenantId, List<Long> ids);

  boolean existsByTenantIdAndCategoryIdAndLanguageAndSlug(
      Long tenantId, Long categoryId, Language language, String slug);
}
