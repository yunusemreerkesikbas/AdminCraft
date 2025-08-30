package com.backend.application.service;

import com.backend.domain.entity.PageCategory;

import java.util.List;
import java.util.Optional;

public interface PageCategoryService {
  PageCategory create(PageCategory category);

  PageCategory update(PageCategory category);

  void delete(Long id);

  Optional<PageCategory> findById(Long id);

  List<PageCategory> listByTenant(Long tenantId);

  List<PageCategory> listChildren(Long tenantId, Long parentId);

  // New Sprint 5 operations (localized)
  List<com.backend.presentation.dto.response.PageCategoryDto> getTree(Long tenantId, String languageCode, Long rootId,
      Integer depth);

  void move(Long tenantId, Long categoryId, Long newParentId);

  void reorder(Long tenantId, Long parentId, List<Long> orderedIds);

  List<com.backend.presentation.dto.response.PageCategoryDto> listChildrenLocalized(Long tenantId, Long parentId,
      String languageCode);
}
