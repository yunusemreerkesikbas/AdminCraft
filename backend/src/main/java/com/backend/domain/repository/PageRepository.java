package com.backend.domain.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PageRepository {

  Page save(Page page);

  List<Page> saveAll(Iterable<Page> pages);

  Optional<Page> findById(Long id);

  void deleteById(Long id);

  boolean existsById(Long id);

  Optional<Page> findByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language);

  boolean existsByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language);

  List<Page> findByTenantId(Long tenantId);

  List<Page> findByTenantIdAndLanguage(Long tenantId, Language language);

  List<Page> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

  List<Page> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, PageStatus status);

  List<Page> findByStatusAndScheduledAtBefore(PageStatus status, LocalDateTime dateTime);
}
