package com.backend.application.service;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PageService {
  Page create(Page page);

  Page update(Page page);

  void delete(Long id);

  Optional<Page> findById(Long id);

  Optional<Page> findBySlug(Long tenantId, String slug, Language language);

  List<Page> listByTenant(Long tenantId);

  List<Page> listByTenantAndLanguage(Long tenantId, Language language);

  List<Page> listByCategory(Long tenantId, Long categoryId);

  Page publish(Long pageId, Long userId);

  Page unpublish(Long pageId, Long userId);

  Page schedule(Long pageId, LocalDateTime when, Long userId);

  String generateUniqueSlug(String title, Long tenantId, Language language);

  int publishDueScheduledPages(LocalDateTime now);
}
