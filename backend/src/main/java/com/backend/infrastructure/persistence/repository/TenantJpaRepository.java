package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantJpaRepository extends JpaRepository<Tenant, Long> {
    
    Optional<Tenant> findBySubdomain(String subdomain);
    
    Optional<Tenant> findByDatabaseName(String databaseName);
    
    Optional<Tenant> findByCustomDomain(String customDomain);
    
    List<Tenant> findByStatus(TenantStatus status);
    
    List<Tenant> findByAdminEmail(String adminEmail);
    
    boolean existsBySubdomain(String subdomain);
    
    boolean existsByDatabaseName(String databaseName);
    
    boolean existsByCustomDomain(String customDomain);
    
    boolean existsByCustomDomainAndIdNot(String customDomain, Long id);
    
    long countByStatus(TenantStatus status);
    
    @Query("SELECT t FROM Tenant t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Tenant> findByStatusOrderByCreatedAtDesc(@Param("status") TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE t.storageUsedMb > :threshold")
    List<Tenant> findTenantsExceedingStorageThreshold(@Param("threshold") Long threshold);

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.activatedAt >= CURRENT_DATE")
    long countTenantsActivatedToday();
}