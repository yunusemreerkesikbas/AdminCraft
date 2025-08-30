package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategoryTranslation;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageCategoryTranslationJpaRepository extends JpaRepository<PageCategoryTranslation, Long> {
  Optional<PageCategoryTranslation> findByTenantIdAndCategoryIdAndLanguage(
      Long tenantId, Long categoryId, Language language);

  List<PageCategoryTranslation> findByTenantIdAndCategoryIdInAndLanguage(
      Long tenantId, List<Long> ids, Language language);

  List<PageCategoryTranslation> findByTenantIdAndCategoryIdIn(Long tenantId, List<Long> ids);

  boolean existsByTenantIdAndCategoryIdAndLanguageAndSlug(
      Long tenantId, Long categoryId, Language language, String slug);
}
