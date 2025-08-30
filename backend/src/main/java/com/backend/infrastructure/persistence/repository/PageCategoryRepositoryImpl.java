package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.repository.PageCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageCategoryRepositoryImpl implements PageCategoryRepository {

  private final PageCategoryJpaRepository jpa;

  @Override
  public PageCategory save(PageCategory category) {
    return jpa.save(category);
  }

  @Override
  public List<PageCategory> saveAll(Iterable<PageCategory> categories) {
    return jpa.saveAll(categories);
  }

  @Override
  public Optional<PageCategory> findById(Long id) {
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
  public boolean existsByTenantIdAndSlug(Long tenantId, String slug) {
    return jpa.existsByTenantIdAndSlug(tenantId, slug);
  }

  @Override
  public Optional<PageCategory> findByTenantIdAndSlug(Long tenantId, String slug) {
    return jpa.findByTenantIdAndSlug(tenantId, slug);
  }

  @Override
  public List<PageCategory> findByTenantId(Long tenantId) {
    return jpa.findByTenantId(tenantId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentId(Long tenantId, Long parentId) {
    return jpa.findByTenantIdAndParentId(tenantId, parentId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentIdOrderBySortOrderAsc(Long tenantId, Long parentId) {
    return jpa.findByTenantIdAndParentIdOrderBySortOrderAsc(tenantId, parentId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(Long tenantId) {
    return jpa.findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(tenantId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndPathStartingWith(Long tenantId, String path) {
    return jpa.findByTenantIdAndPathStartingWith(tenantId, path);
  }
}
