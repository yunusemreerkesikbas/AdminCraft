package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategoryTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.PageCategoryTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageCategoryTranslationRepositoryImpl implements PageCategoryTranslationRepository {

  private final PageCategoryTranslationJpaRepository jpa;

  @Override
  public PageCategoryTranslation save(PageCategoryTranslation translation) {
    return jpa.save(translation);
  }

  @Override
  public List<PageCategoryTranslation> saveAll(Iterable<PageCategoryTranslation> translations) {
    return jpa.saveAll(translations);
  }

  @Override
  public Optional<PageCategoryTranslation> findByTenantIdAndCategoryIdAndLanguage(Long tenantId,
      Long categoryId,
      Language language) {
    return jpa.findByTenantIdAndCategoryIdAndLanguage(tenantId, categoryId, language);
  }

  @Override
  public List<PageCategoryTranslation> findByTenantIdAndCategoryIdInAndLanguage(Long tenantId,
      List<Long> categoryIds,
      Language language) {
    return jpa.findByTenantIdAndCategoryIdInAndLanguage(tenantId, categoryIds, language);
  }

  @Override
  public List<PageCategoryTranslation> findByTenantIdAndCategoryIdIn(Long tenantId, List<Long> ids) {
    return jpa.findByTenantIdAndCategoryIdIn(tenantId, ids);
  }

  @Override
  public boolean existsByTenantIdAndCategoryIdAndLanguageAndSlug(Long tenantId,
      Long categoryId,
      Language language,
      String slug) {
    return jpa.existsByTenantIdAndCategoryIdAndLanguageAndSlug(tenantId, categoryId, language, slug);
  }
}
