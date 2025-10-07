package com.backend.domain.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByUuid(String uuid);

    Optional<Page> findByTenantIdAndUid(Long tenantId, String uid);

    List<Page> findByTenantId(Long tenantId);

    List<Page> findByTenantIdAndStatus(Long tenantId, PageStatus status);

    List<Page> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    @Query("SELECT p FROM Page p WHERE p.tenantId = :tenantId ORDER BY p.sortOrder ASC")
    List<Page> findByTenantIdOrderBySortOrder(@Param("tenantId") Long tenantId);

    boolean existsByTenantIdAndUid(Long tenantId, String uid);

    long countByTenantId(Long tenantId);
}
