package com.backend.infrastructure.persistence.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.Tenant;

@Repository
public interface TenantPlatformRepository extends JpaRepository<Tenant, Long> {

  Optional<Tenant> findBySubdomain(String subdomain);

  Optional<Tenant> findByDatabaseName(String databaseName);

  Optional<Tenant> findByCustomDomain(String customDomain);

  boolean existsBySubdomain(String subdomain);

  boolean existsByCustomDomainAndIdNot(String customDomain, Long id);

  long countByStatus(String status);

  java.util.List<Tenant> findByStatus(String status);

  java.util.List<Tenant> findByStatusOrderByCreatedAtDesc(String status);

  @Query("SELECT t FROM Tenant t WHERE t.storageUsedMb > :threshold")
  java.util.List<Tenant> findTenantsExceedingStorageThreshold(Long threshold);

  @Query("SELECT COUNT(t) FROM Tenant t WHERE t.activatedAt >= CURRENT_DATE")
  long countTenantsActivatedToday();

  java.util.List<Tenant> findTop5ByOrderByCreatedAtDesc();

  @Query("SELECT COALESCE(SUM(t.storageUsedMb), 0) FROM Tenant t")
  long sumTotalStorageMb();

  @Query("""
      SELECT t FROM Tenant t
      WHERE (:status IS NULL OR t.status = :status)
        AND (
          :search IS NULL OR
          LOWER(t.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR
          LOWER(t.subdomain) LIKE LOWER(CONCAT('%', :search, '%')) OR
          LOWER(COALESCE(t.customDomain, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        )
      """)
  Page<Tenant> searchTenants(
      @Param("status") String status,
      @Param("search") String search,
      Pageable pageable);
}
