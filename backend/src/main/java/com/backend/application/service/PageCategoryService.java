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
}
