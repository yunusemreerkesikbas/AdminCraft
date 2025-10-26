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
  public Optional<PageCategory> findByIdAndTenantId(Long id, Long tenantId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findById(id);
  }

  @Override
  public void delete(PageCategory category) {
    jpa.delete(category);
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
  public boolean existsByTenantIdAndUid(Long tenantId, String uid) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.existsByUid(uid);
  }

  @Override
  public List<PageCategory> findByTenantIdOrderBySortOrderAsc(Long tenantId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findAllByOrderBySortOrderAsc();
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentId(Long tenantId, Long parentId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByParentId(parentId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentIdOrderBySortOrderAsc(Long tenantId, Long parentId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByParentIdOrderBySortOrderAsc(parentId);
  }

  @Override
  public List<PageCategory> findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(Long tenantId) {
    // In database-per-tenant model, routing is handled by TenantContext
    return jpa.findByParentIdIsNullOrderBySortOrderAsc();
  }
}
