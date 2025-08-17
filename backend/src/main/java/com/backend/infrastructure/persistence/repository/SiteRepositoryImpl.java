package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiteRepositoryImpl implements SiteRepository {
    
    private final SiteJpaRepository siteJpaRepository;
    
    @Override
    public Site save(Site site) {
        return siteJpaRepository.save(site);
    }
    
    @Override
    public List<Site> saveAll(Iterable<Site> sites) {
        return siteJpaRepository.saveAll(sites);
    }
    
    @Override
    public Optional<Site> findById(Long id) {
        return siteJpaRepository.findById(id);
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
    public boolean existsById(Long id) {
        return siteJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return siteJpaRepository.count();
    }
    
    @Override
    public void deleteAllById(Iterable<Long> ids) {
        siteJpaRepository.deleteAllById(ids);
    }
    
    @Override
    public List<Site> findAllByOrderByCreatedAtDesc() {
        return siteJpaRepository.findAllByOrderByCreatedAtDesc();
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
    public long countByTenantId(Long tenantId) {
        return siteJpaRepository.countByTenantId(tenantId);
    }
    
    @Override
    public Optional<Site> findByDomain(String domain) {
        return siteJpaRepository.findByDomain(domain);
    }
    
    @Override
    public Optional<Site> findByDomainIgnoreCase(String domain) {
        return siteJpaRepository.findByDomainIgnoreCase(domain);
    }
    
    @Override
    public Optional<Site> findByCustomDomain(String customDomain) {
        return siteJpaRepository.findByCustomDomain(customDomain);
    }
    
    @Override
    public boolean existsByDomain(String domain) {
        return siteJpaRepository.existsByDomain(domain);
    }
    
    @Override
    public boolean existsByDomainIgnoreCase(String domain) {
        return siteJpaRepository.existsByDomainIgnoreCase(domain);
    }
    
    @Override
    public boolean existsByCustomDomain(String customDomain) {
        return siteJpaRepository.existsByCustomDomain(customDomain);
    }
    
    @Override
    public boolean existsByCustomDomainIgnoreCase(String customDomain) {
        return siteJpaRepository.existsByCustomDomainIgnoreCase(customDomain);
    }
    
    @Override
    public boolean existsBySiteNameIgnoreCaseAndTenantId(String siteName, Long tenantId) {
        return siteJpaRepository.existsBySiteNameIgnoreCaseAndTenantId(siteName, tenantId);
    }
    
    @Override
    public List<Site> findByPublishedTrue() {
        return siteJpaRepository.findByPublishedTrue();
    }
    
    @Override
    public List<Site> findByTenantIdAndPublishedTrue(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndPublishedTrue(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndPublishedFalse(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndPublishedFalse(tenantId);
    }
    
    @Override
    public List<Site> findByMaintenanceModeTrue() {
        return siteJpaRepository.findByMaintenanceModeTrue();
    }
    
    @Override
    public List<Site> findByTenantIdAndMaintenanceModeTrue(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndMaintenanceModeTrue(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndEnabledLanguagesContaining(Long tenantId, Language language) {
        return siteJpaRepository.findByTenantIdAndEnabledLanguagesContaining(tenantId, language);
    }
    
    @Override
    public List<Site> findByDefaultLanguage(Language language) {
        return siteJpaRepository.findByDefaultLanguage(language);
    }
    
    @Override
    public List<Site> findByTenantIdAndDefaultLanguage(Long tenantId, Language language) {
        return siteJpaRepository.findByTenantIdAndDefaultLanguage(tenantId, language);
    }
    
    @Override
    public List<Site> findByThemeName(String themeName) {
        return siteJpaRepository.findByThemeName(themeName);
    }
    
    @Override
    public List<Site> findByTenantIdAndThemeName(Long tenantId, String themeName) {
        return siteJpaRepository.findByTenantIdAndThemeName(tenantId, themeName);
    }
    
    @Override
    public List<Site> findBySslEnabledTrue() {
        return siteJpaRepository.findBySslEnabledTrue();
    }
    
    @Override
    public List<Site> findByTenantIdAndSslEnabledTrue(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndSslEnabledTrue(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndSslEnabledFalse(Long tenantId) {
        return siteJpaRepository.findByTenantIdAndSslEnabledFalse(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdAndSiteNameContainingIgnoreCase(Long tenantId, String siteName) {
        return siteJpaRepository.findByTenantIdAndSiteNameContainingIgnoreCase(tenantId, siteName);
    }
    
    @Override
    public List<Site> findByTenantIdAndDescriptionContainingIgnoreCase(Long tenantId, String description) {
        return siteJpaRepository.findByTenantIdAndDescriptionContainingIgnoreCase(tenantId, description);
    }
    
    @Override
    public List<Site> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<Site> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
    }
    
    @Override
    public List<Site> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByPublishedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<Site> findByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return siteJpaRepository.findByTenantIdAndPublishedAtBetween(tenantId, startDate, endDate);
    }
    
    @Override
    public List<Site> findByTenantIdWithAnalytics(Long tenantId) {
        return siteJpaRepository.findByTenantIdWithAnalytics(tenantId);
    }
    
    @Override
    public List<Site> findByTenantIdWithTagManager(Long tenantId) {
        return siteJpaRepository.findByTenantIdWithTagManager(tenantId);
    }
    
    @Override
    public List<Site> findActiveSitesByTenantId(Long tenantId) {
        return siteJpaRepository.findActiveSitesByTenantId(tenantId);
    }
    
    @Override
    public List<Site> findAllActiveSites() {
        return siteJpaRepository.findAllActiveSites();
    }
    
    @Override
    public long countPublishedSitesByTenantId(Long tenantId) {
        return siteJpaRepository.countPublishedSitesByTenantId(tenantId);
    }
    
    @Override
    public List<Site> findRecentlyPublishedSitesByTenantId(Long tenantId) {
        return siteJpaRepository.findRecentlyPublishedSitesByTenantId(tenantId);
    }
    
    @Override
    public void deleteByTenantId(Long tenantId) {
        siteJpaRepository.deleteByTenantId(tenantId);
    }
    
    @Override
    public List<Site> findByIdIn(List<Long> ids) {
        return siteJpaRepository.findByIdIn(ids);
    }
}