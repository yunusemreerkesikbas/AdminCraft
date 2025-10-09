package com.backend.domain.repository;

import com.backend.domain.entity.PageCategory;

import java.util.List;
import java.util.Optional;

public interface PageCategoryRepository {
  PageCategory save(PageCategory category);

  List<PageCategory> saveAll(Iterable<PageCategory> categories);

  Optional<PageCategory> findById(Long id);

  Optional<PageCategory> findByIdAndTenantId(Long id, Long tenantId);

  void delete(PageCategory category);

  void deleteById(Long id);

  boolean existsById(Long id);

  boolean existsByTenantIdAndUid(Long tenantId, String uid);

  List<PageCategory> findByTenantIdOrderBySortOrderAsc(Long tenantId);

  List<PageCategory> findByTenantIdAndParentId(Long tenantId, Long parentId);

  List<PageCategory> findByTenantIdAndParentIdOrderBySortOrderAsc(Long tenantId, Long parentId);

  List<PageCategory> findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(Long tenantId);
}
