package com.backend.application.service;

import com.backend.domain.entity.Site;
import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteServiceImpl implements SiteService {
    
    private final SiteRepository siteRepository;
    private final MenuRepository menuRepository;
    
    @Override
    public Site createSite(Site site) {
        log.debug("Creating new site with name: {}", site.getSiteName());
        
        // Set defaults
        if (site.getIsActive() == null) {
            site.setIsActive(true);
        }
        if (site.getIsPublished() == null) {
            site.setIsPublished(false);
        }
        if (site.getTheme() == null) {
            site.setTheme("default");
        }
        
        Site savedSite = siteRepository.save(site);
        log.info("Site created successfully with ID: {}", savedSite.getId());
        
        return savedSite;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Site> getSiteById(Long id) {
        return siteRepository.findById(id);
    }
    
    @Override
    public Site updateSite(Site site) {
        log.debug("Updating site with ID: {}", site.getId());
        
        if (!siteRepository.existsById(site.getId())) {
            throw new IllegalArgumentException("Site not found with ID: " + site.getId());
        }
        
        Site updatedSite = siteRepository.save(site);
        log.info("Site updated successfully with ID: {}", updatedSite.getId());
        
        return updatedSite;
    }
    
    @Override
    public void deleteSite(Long id) {
        log.debug("Deleting site with ID: {}", id);
        
        if (!siteRepository.existsById(id)) {
            throw new IllegalArgumentException("Site not found with ID: " + id);
        }
        
        siteRepository.deleteById(id);
        log.info("Site deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Site> getSitesByTenantId(Long tenantId) {
        return siteRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Site> getDefaultSiteByTenantId(Long tenantId) {
        return siteRepository.findFirstByTenantIdAndIsActiveTrue(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countSitesByTenantId(Long tenantId) {
        return siteRepository.countByTenantId(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Site> getSiteByDomain(String domain) {
        return siteRepository.findByDomain(domain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isDomainAvailable(String domain) {
        return !siteRepository.existsByDomain(domain);
    }
    
    @Override
    public Site updateDomain(Long siteId, String domain) {
        Site site = siteRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        
        if (domain != null && !domain.trim().isEmpty() && siteRepository.existsByDomainAndIdNot(domain, siteId)) {
            throw new IllegalArgumentException("Domain already taken: " + domain);
        }
        
        site.setDomain(domain);
        return siteRepository.save(site);
    }
    
    @Override
    public Site publishSite(Long siteId) {
        Site site = siteRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        
        if (!canPublish(siteId)) {
            throw new IllegalStateException("Site cannot be published in current state");
        }
        
        site.setIsPublished(true);
        site.setPublishedAt(LocalDateTime.now());
        
        Site publishedSite = siteRepository.save(site);
        log.info("Site published: {}", siteId);
        
        return publishedSite;
    }
    
    @Override
    public Site unpublishSite(Long siteId) {
        Site site = siteRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        
        site.setIsPublished(false);
        site.setPublishedAt(null);
        
        Site unpublishedSite = siteRepository.save(site);
        log.info("Site unpublished: {}", siteId);
        
        return unpublishedSite;
    }
    
    @Override
    public Site activateSite(Long siteId) {
        Site site = siteRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        
        site.setIsActive(true);
        
        Site activatedSite = siteRepository.save(site);
        log.info("Site activated: {}", siteId);
        
        return activatedSite;
    }
    
    @Override
    public Site deactivateSite(Long siteId) {
        Site site = siteRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        
        site.setIsActive(false);
        
        Site deactivatedSite = siteRepository.save(site);
        log.info("Site deactivated: {}", siteId);
        
        return deactivatedSite;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Site> getPublishedSites(Long tenantId) {
        return siteRepository.findByTenantIdAndIsPublishedTrue(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Site> getActiveSites(Long tenantId) {
        return siteRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }
    
    @Override
    public List<Menu> getMenusBySiteId(Long siteId) {
        return menuRepository.findBySiteIdOrderByLanguageAsc(siteId);
    }
    
    @Override
    public List<Menu> getMenusBySiteIdAndLanguage(Long siteId, Language language) {
        return menuRepository.findBySiteIdAndLanguage(siteId, language);
    }
    
    @Override
    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }
    
    @Override
    public Menu updateMenu(Menu menu) {
        if (!menuRepository.existsById(menu.getId())) {
            throw new IllegalArgumentException("Menu not found with ID: " + menu.getId());
        }
        return menuRepository.save(menu);
    }
    
    @Override
    public void deleteMenu(Long menuId) {
        if (!menuRepository.existsById(menuId)) {
            throw new IllegalArgumentException("Menu not found with ID: " + menuId);
        }
        menuRepository.deleteById(menuId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalSitesCount() {
        return siteRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getActiveSitesCount(Long tenantId) {
        return siteRepository.countByTenantIdAndIsActiveTrue(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getPublishedSitesCount(Long tenantId) {
        return siteRepository.countByTenantIdAndIsPublishedTrue(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Site> getRecentSites(Long tenantId, int limit) {
        return siteRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                           .stream()
                           .limit(limit)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> validateSite(Site site) {
        List<String> errors = new ArrayList<>();
        
        if (site.getSiteName() == null || site.getSiteName().trim().isEmpty()) {
            errors.add("Site name is required");
        }
        
        if (site.getDefaultLanguage() == null) {
            errors.add("Default language is required");
        }
        
        if (site.getTenantId() == null) {
            errors.add("Tenant ID is required");
        }
        
        return errors;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canPublish(Long siteId) {
        Optional<Site> siteOpt = siteRepository.findById(siteId);
        if (siteOpt.isEmpty()) {
            return false;
        }
        
        Site site = siteOpt.get();
        return site.getSiteName() != null && !site.getSiteName().trim().isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canActivate(Long siteId) {
        return canPublish(siteId);
    }
    
    // Placeholder implementations for interface compliance
    @Override public Site addLanguage(Long siteId, Language language) { return null; }
    @Override public Site removeLanguage(Long siteId, Language language) { return null; }
    @Override public Site setDefaultLanguage(Long siteId, Language language) { return null; }
    @Override public Set<Language> getSupportedLanguages(Long siteId) { return Set.of(); }
    @Override public boolean isLanguageSupported(Long siteId, Language language) { return false; }
    @Override public Site updateTheme(Long siteId, String theme) { return null; }
    @Override public Site updateLogo(Long siteId, String logoUrl) { return null; }
    @Override public Site updateFavicon(Long siteId, String faviconUrl) { return null; }
    @Override public Site updateBranding(Long siteId, String primaryColor, String secondaryColor, String fontFamily) { return null; }
    @Override public List<String> getAvailableThemes() { return List.of("default", "modern", "classic"); }
    @Override public List<Site> getSitesWithCustomDomain(Long tenantId) { return List.of(); }
    @Override public Site updateSeoSettings(Long siteId, String metaTitle, String metaDescription, String metaKeywords) { return null; }
    @Override public Site updateAnalyticsCode(Long siteId, String googleAnalyticsId, String customCode) { return null; }
    @Override public void bulkPublish(List<Long> siteIds) { }
    @Override public void bulkUnpublish(List<Long> siteIds) { }
    @Override public void bulkActivate(List<Long> siteIds) { }
    @Override public void bulkDeactivate(List<Long> siteIds) { }
    @Override public void deleteSitesByTenantId(Long tenantId) { }
}