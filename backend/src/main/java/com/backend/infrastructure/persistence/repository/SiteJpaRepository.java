package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteJpaRepository extends JpaRepository<Site, Long> {
    
    // Basic queries
    Optional<Site> findByDomain(String domain);
    
    // Tenant queries
    List<Site> findByTenantId(Long tenantId);
    List<Site> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    
    // Active status queries
    List<Site> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    List<Site> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Site> findFirstByTenantIdAndIsActiveTrue(Long tenantId);
    List<Site> findByIsActive(Boolean isActive);
    
    // Published status queries
    List<Site> findByTenantIdAndIsPublishedTrue(Long tenantId);
    
    // Existence checks
    boolean existsByDomain(String domain);
    boolean existsByDomainAndIdNot(String domain, Long id);
    boolean existsBySiteNameAndTenantId(String siteName, Long tenantId);
    boolean existsBySiteNameAndTenantIdAndIdNot(String siteName, Long tenantId, Long id);
    
    // Count queries
    long countByTenantId(Long tenantId);
    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    long countByTenantIdAndIsPublishedTrue(Long tenantId);
} 