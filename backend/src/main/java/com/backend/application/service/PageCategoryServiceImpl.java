package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.repository.PageCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PageCategoryServiceImpl implements PageCategoryService {

  private final PageCategoryRepository categoryRepository;

  @Override
  public PageCategory create(PageCategory category) {
    if (categoryRepository.existsByTenantIdAndSlug(category.getTenantId(), category.getSlug())) {
      throw new IllegalArgumentException("Category slug already exists for tenant");
    }
    return categoryRepository.save(category);
  }

  @Override
  public PageCategory update(PageCategory category) {
    PageCategory existing = categoryRepository.findById(category.getId())
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    if (!existing.getSlug().equals(category.getSlug()) &&
        categoryRepository.existsByTenantIdAndSlug(category.getTenantId(), category.getSlug())) {
      throw new IllegalArgumentException("Category slug already exists for tenant");
    }
    return categoryRepository.save(category);
  }

  @Override
  public void delete(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new IllegalArgumentException("Category not found");
    }
    categoryRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PageCategory> findById(Long id) {
    return categoryRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategory> listByTenant(Long tenantId) {
    return categoryRepository.findByTenantId(tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageCategory> listChildren(Long tenantId, Long parentId) {
    return categoryRepository.findByTenantIdAndParentId(tenantId, parentId);
  }
}
