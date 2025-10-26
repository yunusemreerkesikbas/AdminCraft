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
  public Optional<PageCategoryI18n> findByCategoryIdAndLanguage(Long categoryId, Language language) {
    return jpa.findByCategory_IdAndLanguage(categoryId, language);
  }

  @Override
  public List<PageCategoryI18n> findByCategoryIdInAndLanguage(
      List<Long> categoryIds, Language language) {
    return jpa.findByCategory_IdInAndLanguage(categoryIds, language);
  }

  @Override
  public List<PageCategoryI18n> findByCategoryId(Long categoryId) {
    return jpa.findByCategory_Id(categoryId);
  }

  @Override
  public List<PageCategoryI18n> findByCategoryIdIn(List<Long> categoryIds) {
    return jpa.findByCategory_IdIn(categoryIds);
  }

  @Override
  public boolean existsByLanguageAndUrl(Language language, String url) {
    return jpa.existsByLanguageAndUrl(language, url);
  }

  @Override
  public boolean existsByLanguageAndUrlAndCategoryIdNot(
      Language language, String url, Long categoryId) {
    return jpa.existsByLanguageAndUrlAndCategory_IdNot(language, url, categoryId);
  }
}
