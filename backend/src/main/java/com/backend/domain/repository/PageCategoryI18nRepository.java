package com.backend.domain.repository;

import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Optional;

public interface PageCategoryI18nRepository {
  PageCategoryI18n save(PageCategoryI18n translation);

  List<PageCategoryI18n> saveAll(Iterable<PageCategoryI18n> translations);

  Optional<PageCategoryI18n> findByCategoryIdAndLanguage(Long categoryId, Language language);

  List<PageCategoryI18n> findByCategoryIdInAndLanguage(
      List<Long> categoryIds, Language language);

  List<PageCategoryI18n> findByCategoryId(Long categoryId);

  List<PageCategoryI18n> findByCategoryIdIn(List<Long> categoryIds);

  boolean existsByLanguageAndUrl(Language language, String url);

  boolean existsByLanguageAndUrlAndCategoryIdNot(
      Language language, String url, Long categoryId);
}
