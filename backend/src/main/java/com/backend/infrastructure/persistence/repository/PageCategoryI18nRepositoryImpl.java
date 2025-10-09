package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.PageCategoryI18nRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageCategoryI18nRepositoryImpl implements PageCategoryI18nRepository {

  private final PageCategoryI18nJpaRepository jpa;

  @Override
  public PageCategoryI18n save(PageCategoryI18n translation) {
    return jpa.save(translation);
  }

  @Override
  public List<PageCategoryI18n> saveAll(Iterable<PageCategoryI18n> translations) {
    return jpa.saveAll(translations);
  }

  @Override
  public Optional<PageCategoryI18n> findByTenantIdAndCategoryIdAndLanguage(Long tenantId,
      Long categoryId,
      Language language) {
    return jpa.findByTenantIdAndCategoryIdAndLanguage(tenantId, categoryId, language);
  }

  @Override
  public List<PageCategoryI18n> findByTenantIdAndCategoryIdInAndLanguage(Long tenantId,
      List<Long> categoryIds,
      Language language) {
    return jpa.findByTenantIdAndCategoryIdInAndLanguage(tenantId, categoryIds, language);
  }

  @Override
  public List<PageCategoryI18n> findByTenantIdAndCategoryId(Long tenantId, Long categoryId) {
    return jpa.findByTenantIdAndCategoryId(tenantId, categoryId);
  }

  @Override
  public boolean existsByTenantIdAndLanguageAndUrl(Long tenantId, Language language, String url) {
    return jpa.existsByTenantIdAndLanguageAndUrl(tenantId, language, url);
  }

  @Override
  public boolean existsByTenantIdAndLanguageAndUrlAndCategoryIdNot(
      Long tenantId, Language language, String url, Long categoryId) {
    return jpa.existsByTenantIdAndLanguageAndUrlAndCategoryIdNot(tenantId, language, url, categoryId);
  }
}
