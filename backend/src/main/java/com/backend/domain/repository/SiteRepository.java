package com.backend.domain.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface SiteRepository {
    
    Site save(Site site);
    
    Optional<Site> findById(Long id);
    
    boolean existsById(Long id);
    
    Optional<Site> findByDomain(String domain);
    
    List<Site> findByTenantId(Long tenantId);
    
    List<Site> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    
    List<Site> findByTenantIdAndIsActive(Long tenantId, boolean isActive);
    
    List<Site> findByTenantIdAndIsActiveTrue(Long tenantId);
    
    Optional<Site> findFirstByTenantIdAndIsActiveTrue(Long tenantId);
    
    List<Site> findByTenantIdAndIsPublishedTrue(Long tenantId);
    
    List<Site> findByIsActive(boolean isActive);
    
    List<Site> findAll();
    
    void deleteById(Long id);
    
    long count();
    
    boolean existsByDomain(String domain);
    
    boolean existsByDomainAndIdNot(String domain, Long id);
    
    boolean existsBySiteNameAndTenantId(String siteName, Long tenantId);
    
    boolean existsBySiteNameAndTenantIdAndIdNot(String siteName, Long tenantId, Long id);
    
    long countByTenantId(Long tenantId);
    
    long countByTenantIdAndIsActive(Long tenantId, boolean isActive);
    
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    
    long countByTenantIdAndIsPublishedTrue(Long tenantId);
}