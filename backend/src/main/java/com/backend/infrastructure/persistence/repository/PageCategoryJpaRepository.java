package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageCategoryJpaRepository extends JpaRepository<PageCategory, Long> {
  boolean existsByTenantIdAndSlug(Long tenantId, String slug);

  Optional<PageCategory> findByTenantIdAndSlug(Long tenantId, String slug);
  
  Optional<PageCategory> findByIdAndTenantId(Long id, Long tenantId);

  List<PageCategory> findByTenantId(Long tenantId);

  List<PageCategory> findByTenantIdAndParentId(Long tenantId, Long parentId);

  List<PageCategory> findByTenantIdAndParentIdOrderBySortOrderAsc(Long tenantId, Long parentId);

  List<PageCategory> findByTenantIdAndParentIdIsNullOrderBySortOrderAsc(Long tenantId);

  List<PageCategory> findByTenantIdAndPathStartingWith(Long tenantId, String path);
}
