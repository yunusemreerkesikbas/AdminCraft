package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageJpaRepository extends JpaRepository<Page, Long> {

  Optional<Page> findByUuid(String uuid);

  Optional<Page> findByTenantIdAndUid(Long tenantId, String uid);

  List<Page> findByTenantId(Long tenantId);

  List<Page> findByTenantIdAndStatus(Long tenantId, PageStatus status);

  List<Page> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

  boolean existsByTenantIdAndUid(Long tenantId, String uid);

  long countByTenantId(Long tenantId);
}
