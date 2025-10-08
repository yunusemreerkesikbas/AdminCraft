package com.backend.application.service;

import com.backend.domain.entity.PageCategory;

import java.util.List;
import java.util.Optional;

public interface PageCategoryService {
    PageCategory create(PageCategory category);

    PageCategory update(PageCategory category);

    void delete(Long id);

    Optional<PageCategory> findById(Long id);

    // Güvenlik: Tenant-aware method'lar
    Optional<PageCategory> findByIdAndTenantId(Long id, Long tenantId);

    void validateParentBelongsToTenant(Long parentId, Long tenantId);

    List<PageCategory> listByTenant(Long tenantId);

    List<PageCategory> listChildren(Long tenantId, Long parentId);

}
