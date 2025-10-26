package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageCategoryI18nJpaRepository extends JpaRepository<PageCategoryI18n, Long> {
  Optional<PageCategoryI18n> findByCategory_IdAndLanguage(Long categoryId, Language language);

  List<PageCategoryI18n> findByCategory_IdInAndLanguage(List<Long> ids, Language language);

  List<PageCategoryI18n> findByCategory_Id(Long categoryId);

  List<PageCategoryI18n> findByCategory_IdIn(List<Long> categoryIds);

  boolean existsByLanguageAndUrl(Language language, String url);

  boolean existsByLanguageAndUrlAndCategory_IdNot(
      Language language, String url, Long categoryId);
}
