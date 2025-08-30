package com.backend.application.service;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageServiceImpl implements PageService {

  private final PageRepository pageRepository;

  @Override
  public Page create(Page page) {
    if (pageRepository.existsByTenantIdAndSlugAndLanguage(page.getTenantId(), page.getSlug(), page.getLanguage())) {
      throw new IllegalArgumentException("Page slug already exists for tenant and language");
    }
    if (page.getCategoryId() == null) {
      throw new IllegalArgumentException("Page category is required");
    }
    return pageRepository.save(page);
  }

  @Override
  public Page update(Page page) {
    Page existing = pageRepository.findById(page.getId())
        .orElseThrow(() -> new PageNotFoundException(page.getId()));

    // Enforce unique slug rule when slug changes
    if (!existing.getSlug().equals(page.getSlug()) &&
        pageRepository.existsByTenantIdAndSlugAndLanguage(page.getTenantId(), page.getSlug(), page.getLanguage())) {
      throw new IllegalArgumentException("Page slug already exists for tenant and language");
    }

    // Copy mutable fields onto the managed entity. Preserve audit.createdBy
    existing.setTenantId(page.getTenantId());
    existing.setTitle(page.getTitle());
    existing.setSlug(page.getSlug());
    existing.setLanguage(page.getLanguage());
    existing.setCategoryId(page.getCategoryId());
    existing.setMetaTitle(page.getMetaTitle());
    existing.setMetaDescription(page.getMetaDescription());
    existing.setCanonicalUrl(page.getCanonicalUrl());
    existing.setUpdatedBy(page.getUpdatedBy());

    return pageRepository.save(existing);
  }

  @Override
  public void delete(Long id) {
    if (!pageRepository.existsById(id)) {
      throw new PageNotFoundException(id);
    }
    pageRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Page> findById(Long id) {
    return pageRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Page> findBySlug(Long tenantId, String slug, Language language) {
    return pageRepository.findByTenantIdAndSlugAndLanguage(tenantId, slug, language);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Page> listByTenant(Long tenantId) {
    return pageRepository.findByTenantId(tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Page> listByTenantAndLanguage(Long tenantId, Language language) {
    return pageRepository.findByTenantIdAndLanguage(tenantId, language);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Page> listByCategory(Long tenantId, Long categoryId) {
    return pageRepository.findByTenantIdAndCategoryId(tenantId, categoryId);
  }

  @Override
  public Page publish(Long pageId, Long userId) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new PageNotFoundException(pageId));
    page.publish(userId); // Use domain logic
    return pageRepository.save(page);
  }

  @Override
  public Page unpublish(Long pageId, Long userId) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new PageNotFoundException(pageId));
    page.unpublish(userId); // Use domain logic
    return pageRepository.save(page);
  }

  @Override
  public Page schedule(Long pageId, LocalDateTime when, Long userId) {
    Page page = pageRepository.findById(pageId)
        .orElseThrow(() -> new PageNotFoundException(pageId));
    page.schedule(when, userId); // Use domain logic
    return pageRepository.save(page);
  }

  @Override
  @Transactional(readOnly = true)
  public String generateUniqueSlug(String title, Long tenantId, Language language) {
    String base = title.toLowerCase()
        .replaceAll("[^a-zA-Z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
    String slug = base;
    int i = 1;
    while (pageRepository.existsByTenantIdAndSlugAndLanguage(tenantId, slug, language)) {
      slug = base + "-" + i;
      i++;
    }
    return slug;
  }

  @Override
  public int publishDueScheduledPages(LocalDateTime now) {
    int[] count = { 0 };
    pageRepository.findByStatusAndScheduledAtBefore(PageStatus.SCHEDULED, now)
        .forEach(p -> {
          p.setStatus(PageStatus.PUBLISHED);
          p.setPublishedAt(now);
          p.setScheduledAt(null);
          pageRepository.save(p);
          count[0]++;
        });
    log.info("Published {} scheduled pages", count[0]);
    return count[0];
  }
}
