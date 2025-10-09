package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageCategoryI18nJpaRepository extends JpaRepository<PageCategoryI18n, Long> {
  Optional<PageCategoryI18n> findByTenantIdAndCategory_IdAndLanguage(
      Long tenantId, Long categoryId, Language language);

  List<PageCategoryI18n> findByTenantIdAndCategory_IdInAndLanguage(
      Long tenantId, List<Long> ids, Language language);

  List<PageCategoryI18n> findByTenantIdAndCategory_Id(Long tenantId, Long categoryId);

  List<PageCategoryI18n> findByTenantIdAndCategory_IdIn(Long tenantId, List<Long> categoryIds);

  boolean existsByTenantIdAndLanguageAndUrl(Long tenantId, Language language, String url);

  boolean existsByTenantIdAndLanguageAndUrlAndCategory_IdNot(
      Long tenantId, Language language, String url, Long categoryId);
}
