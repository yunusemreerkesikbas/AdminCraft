package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageRepositoryImpl implements PageRepository {

  private final PageJpaRepository jpa;

  @Override
  public Page save(Page page) {
    return jpa.save(page);
  }

  @Override
  public List<Page> saveAll(Iterable<Page> pages) {
    return jpa.saveAll(pages);
  }

  @Override
  public Optional<Page> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public void deleteById(Long id) {
    jpa.deleteById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return jpa.existsById(id);
  }

  @Override
  public Optional<Page> findByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language) {
    return jpa.findByTenantIdAndSlugAndLanguage(tenantId, slug, language);
  }

  @Override
  public boolean existsByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language) {
    return jpa.existsByTenantIdAndSlugAndLanguage(tenantId, slug, language);
  }

  @Override
  public List<Page> findByTenantId(Long tenantId) {
    return jpa.findByTenantId(tenantId);
  }

  @Override
  public List<Page> findByTenantIdAndLanguage(Long tenantId, Language language) {
    return jpa.findByTenantIdAndLanguage(tenantId, language);
  }

  @Override
  public List<Page> findByTenantIdAndCategoryId(Long tenantId, Long categoryId) {
    return jpa.findByTenantIdAndCategoryId(tenantId, categoryId);
  }

  @Override
  public List<Page> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, PageStatus status) {
    return jpa.findByTenantIdAndLanguageAndStatus(tenantId, language, status);
  }

  @Override
  public List<Page> findByStatusAndScheduledAtBefore(PageStatus status, LocalDateTime dateTime) {
    return jpa.findByStatusAndScheduledAtBefore(status, dateTime);
  }
}
