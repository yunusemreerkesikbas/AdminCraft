package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PageJpaRepository extends JpaRepository<Page, Long> {

  Optional<Page> findByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language);

  boolean existsByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language);

  List<Page> findByTenantId(Long tenantId);

  List<Page> findByTenantIdAndLanguage(Long tenantId, Language language);

  List<Page> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

  List<Page> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, PageStatus status);

  List<Page> findByStatusAndScheduledAtBefore(PageStatus status, LocalDateTime dateTime);
}
