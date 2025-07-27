package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SiteRepositoryImpl implements SiteRepository {
    
    private final SiteJpaRepository siteJpaRepository;
    
    @Override
    public Site save(Site site) {
        return siteJpaRepository.save(site);
    }
    
    @Override
    public Optional<Site> findById(Long id) {
        return siteJpaRepository.findById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return siteJpaRepository.existsById(id);
    }
    
    @Override
    public Optional<Site> findByDomain(String domain) {
        return siteJpaRepository.findByDomain(domain);
    }
    
    @Override
    public List<Site> findByTenantId(Long tenantId) {
        return siteJpaRepository.findByTenantId(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdOrderByCreatedAtDesc(Long tenantId) {
        return siteJpaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndIsActive(Long tenantId, boolean isActive) {
        return siteJpaRepository.findByTenantIdAndActive(tenantId, isActive);
    }
    
    @Override
    public List<Site> findByTenantIdAndIsActiveTrue(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndActiveTrue(tenantId);
    }
    
    @Override
    public Optional<Site> findFirstByTenantIdAndIsActiveTrue(Long tenantId) {
        return siteJpaRepository.findFirstByTenantIdAndActiveTrue(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndIsPublishedTrue(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndPublishedTrue(tenantId);
    }
    
    @Override
    public List<Site> findByIsActive(boolean isActive) {
        return siteJpaRepository.findByActive(isActive);
    }
    
    @Override
    public List<Site> findAll() {
        return siteJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        siteJpaRepository.deleteById(id);
    }
    
    @Override
    public long count() {
        return siteJpaRepository.count();
    }
    
    @Override
    public boolean existsByDomain(String domain) {
        return siteJpaRepository.existsByDomain(domain);
    }
    
    @Override
    public boolean existsByDomainAndIdNot(String domain, Long id) {
        return siteJpaRepository.existsByDomainAndIdNot(domain, id);
    }
    
    @Override
    public boolean existsBySiteNameAndTenantId(String siteName, Long tenantId) {
        return siteJpaRepository.existsBySiteNameAndTenantId(siteName, tenantId);
    }
    
    @Override
    public boolean existsBySiteNameAndTenantIdAndIdNot(String siteName, Long tenantId, Long id) {
        return siteJpaRepository.existsBySiteNameAndTenantIdAndIdNot(siteName, tenantId, id);
    }
    
    @Override
    public long countByTenantId(Long tenantId) {
        return siteJpaRepository.countByTenantId(tenantId);
    }
    
    @Override
    public long countByTenantIdAndIsActive(Long tenantId, boolean isActive) {
        return siteJpaRepository.countByTenantIdAndActive(tenantId, isActive);
    }
    
    @Override
    public long countByTenantIdAndIsActiveTrue(Long tenantId) {
        return siteJpaRepository.countByTenantIdAndActiveTrue(tenantId);
    }
    
    @Override
    public long countByTenantIdAndIsPublishedTrue(Long tenantId) {
        return siteJpaRepository.countByTenantIdAndPublishedTrue(tenantId);
    }
} 