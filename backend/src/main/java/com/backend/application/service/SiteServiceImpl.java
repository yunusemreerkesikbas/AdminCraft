package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CreateSiteRequest;
import com.backend.application.dto.request.UpdateSiteRequest;
import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.SiteNotFoundException;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.mapper.SiteMapper;
import com.backend.presentation.dto.response.SiteResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final TenantRepository tenantRepository;
    private final SiteMapper siteMapper;
    private final com.backend.infrastructure.tenant.TenantContext tenantContext;

    @Override
    public SiteResponse createSite(CreateSiteRequest request, Long userId, Language displayLanguage) {
        String tenantIdStr = tenantContext.getTenantId();
        Long tenantId = Long.parseLong(tenantIdStr);

        log.debug("Creating new site for tenant: {}", tenantId);

        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException("Tenant not found with ID: " + tenantId);
        }

        // Validate domain availability
        if (request.domain() != null && !isDomainAvailable(request.domain())) {
            throw new IllegalArgumentException("Domain is already taken: " + request.domain());
        }

        // Validate site name availability
        if (!isSiteNameAvailable(request.siteName())) {
            throw new IllegalArgumentException("Site name is already taken: " + request.siteName());
        }

        // Create new site entity
        Site site = siteMapper.toEntity(request);
        site.setCreatedBy(userId);
        site.setUpdatedBy(userId);
        site.setCreatedAt(LocalDateTime.now());
        site.setUpdatedAt(LocalDateTime.now());

        // Save site
        Site savedSite = siteRepository.save(site);

        log.info("Site created successfully with ID: {} for tenant: {}", savedSite.getId(), tenantId);

        return siteMapper.toResponse(savedSite, displayLanguage);
    }

    @Override
    public Optional<SiteResponse> getSiteById(Long id, Language displayLanguage) {
        log.debug("Getting site by ID: {}", id);

        return siteRepository.findById(id)
                .map(site -> siteMapper.toResponse(site, displayLanguage));
    }

    @Override
    public List<SiteResponse> getSites(Language displayLanguage) {
        log.debug("Getting all sites");

        return siteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getAllSites(Language displayLanguage) {
        log.debug("Getting all sites");

        return siteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public SiteResponse updateSite(Long id, UpdateSiteRequest request, Long userId, Language displayLanguage) {
        log.debug("Updating site with ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        // Update site entity
        siteMapper.updateEntity(site, request);
        site.setUpdatedBy(userId);
        site.setUpdatedAt(LocalDateTime.now());

        // Save updated site
        Site updatedSite = siteRepository.save(site);

        log.info("Site updated successfully with ID: {}", id);

        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public void deleteSite(Long id) {
        log.debug("Deleting site with ID: {}", id);

        if (!siteRepository.existsById(id)) {
            throw new SiteNotFoundException("Site not found with ID: " + id);
        }

        siteRepository.deleteById(id);

        log.info("Site deleted successfully with ID: {}", id);
    }

    @Override
    public SiteResponse publishSite(Long id, Language displayLanguage) {
        log.debug("Publishing site with ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setPublished(true);
        site.setPublishedAt(LocalDateTime.now());
        site.setUpdatedAt(LocalDateTime.now());

        Site publishedSite = siteRepository.save(site);

        log.info("Site published successfully with ID: {}", id);

        return siteMapper.toResponse(publishedSite, displayLanguage);
    }

    @Override
    public SiteResponse unpublishSite(Long id, Language displayLanguage) {
        log.debug("Unpublishing site with ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setPublished(false);
        site.setPublishedAt(null);
        site.setUpdatedAt(LocalDateTime.now());

        Site unpublishedSite = siteRepository.save(site);

        log.info("Site unpublished successfully with ID: {}", id);

        return siteMapper.toResponse(unpublishedSite, displayLanguage);
    }

    @Override
    public SiteResponse enableMaintenanceMode(Long id, String message, Language displayLanguage) {
        log.debug("Enabling maintenance mode for site with ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setMaintenanceMode(true);
        site.setMaintenanceMessage(message);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);

        log.info("Maintenance mode enabled for site with ID: {}", id);

        Site loadedSite = siteRepository.findByIdWithEnabledLanguages(updatedSite.getId())
                .orElse(updatedSite);

        return siteMapper.toResponse(loadedSite, displayLanguage);
    }

    @Override
    public SiteResponse disableMaintenanceMode(Long id, Language displayLanguage) {
        log.debug("Disabling maintenance mode for site with ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setMaintenanceMode(false);
        site.setMaintenanceMessage(null);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);

        log.info("Maintenance mode disabled for site with ID: {}", id);

        Site loadedSite = siteRepository.findByIdWithEnabledLanguages(updatedSite.getId())
                .orElse(updatedSite);

        return siteMapper.toResponse(loadedSite, displayLanguage);
    }

    @Override
    public Optional<SiteResponse> getSiteByDomain(String domain, Language displayLanguage) {
        log.debug("Getting site by domain: {}", domain);

        return siteRepository.findByDomainIgnoreCase(domain)
                .map(site -> siteMapper.toResponse(site, displayLanguage));
    }

    @Override
    public boolean isDomainAvailable(String domain) {
        log.debug("Checking domain availability: {}", domain);

        return !siteRepository.existsByDomainIgnoreCase(domain) &&
                !siteRepository.existsByCustomDomainIgnoreCase(domain);
    }

    @Override
    public boolean isSiteNameAvailable(String siteName) {
        log.debug("Checking site name availability: {}", siteName);

        return !siteRepository.existsBySiteNameIgnoreCase(siteName);
    }

    @Override
    public List<String> getAllSiteUrls() {
        log.debug("Getting all site URLs");

        return siteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(site -> {
                    if (site.getCustomDomain() != null && !site.getCustomDomain().isEmpty()) {
                        return (site.getSslEnabled() ? "https://" : "http://") + site.getCustomDomain();
                    } else if (site.getDomain() != null && !site.getDomain().isEmpty()) {
                        return (site.getSslEnabled() ? "https://" : "http://") + site.getDomain();
                    } else {
                        // Fallback to subdomain pattern if no domain is set
                        return (site.getSslEnabled() ? "https://" : "http://") + site.getSiteName().toLowerCase()
                                + ".yourdomain.com";
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getSiteUrl(Long id) {
        log.debug("Getting site URL for site ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        if (site.getCustomDomain() != null && !site.getCustomDomain().isEmpty()) {
            return (site.getSslEnabled() ? "https://" : "http://") + site.getCustomDomain();
        } else if (site.getDomain() != null && !site.getDomain().isEmpty()) {
            return (site.getSslEnabled() ? "https://" : "http://") + site.getDomain();
        } else {
            // Fallback to subdomain pattern
            return (site.getSslEnabled() ? "https://" : "http://") + site.getSiteName().toLowerCase()
                    + ".yourdomain.com";
        }
    }

    @Override
    public String getFullDomainUrl(Long id) {
        log.debug("Getting full domain URL for site ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        if (site.getCustomDomain() != null && !site.getCustomDomain().isEmpty()) {
            return site.getCustomDomain();
        } else if (site.getDomain() != null && !site.getDomain().isEmpty()) {
            return site.getDomain();
        } else {
            // Fallback to subdomain pattern
            return site.getSiteName().toLowerCase() + ".yourdomain.com";
        }
    }

    @Override
    public SiteResponse getSiteConfiguration(Long id, Language displayLanguage) {
        log.debug("Getting site configuration for site ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        return siteMapper.toResponse(site, displayLanguage);
    }

    @Override
    public SiteResponse updateSiteConfiguration(Long id, UpdateSiteRequest request, Language displayLanguage) {
        log.debug("Updating site configuration for site ID: {}", id);

        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        // Update site configuration using the mapper
        siteMapper.updateEntity(site, request);
        site.setUpdatedAt(LocalDateTime.now());

        // Save updated site
        Site updatedSite = siteRepository.save(site);

        log.info("Site configuration updated successfully for site ID: {}", id);

        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public void deleteSites() {
        log.debug("Deleting all sites for tenant");

        siteRepository.deleteAll();

        log.info("All sites deleted successfully for tenant");
    }

    @Override
    public long countSites() {
        return siteRepository.count();
    }

    @Override
    public List<SiteResponse> getActiveSites(Language displayLanguage) {
        return siteRepository.findActiveSites()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SiteResponse> getSiteByCustomDomain(String customDomain, Language displayLanguage) {
        return siteRepository.findByCustomDomain(customDomain)
                .map(site -> siteMapper.toResponse(site, displayLanguage));
    }

    @Override
    public boolean isCustomDomainAvailable(String customDomain) {
        return !siteRepository.existsByCustomDomainIgnoreCase(customDomain);
    }

    @Override
    public SiteResponse updateDomain(Long id, String domain, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setDomain(domain);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse updateCustomDomain(Long id, String customDomain, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setCustomDomain(customDomain);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public List<SiteResponse> getPublishedSites(Language displayLanguage) {
        return siteRepository.findByPublishedTrue()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getUnpublishedSites(Language displayLanguage) {
        return siteRepository.findByPublishedFalse()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getSitesInMaintenanceMode(Language displayLanguage) {
        return siteRepository.findByMaintenanceModeTrue()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public SiteResponse addEnabledLanguage(Long id, Language language, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.getEnabledLanguages().add(language);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse removeEnabledLanguage(Long id, Language language, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.getEnabledLanguages().remove(language);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse setDefaultLanguage(Long id, Language defaultLanguage, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setDefaultLanguage(defaultLanguage);
        if (!site.getEnabledLanguages().contains(defaultLanguage)) {
            site.getEnabledLanguages().add(defaultLanguage);
        }
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public List<SiteResponse> getSitesByLanguage(Language language, Language displayLanguage) {
        return siteRepository.findByEnabledLanguagesContaining(language)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getSitesByDefaultLanguage(Language defaultLanguage, Language displayLanguage) {
        return siteRepository.findByDefaultLanguage(defaultLanguage)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public SiteResponse updateTheme(Long id, String themeName, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setThemeName(themeName);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public List<SiteResponse> getSitesByTheme(String themeName, Language displayLanguage) {
        return siteRepository.findByThemeName(themeName)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableThemes() {
        // Return a basic list of available themes
        return List.of("default", "modern", "classic", "minimal", "corporate");
    }

    @Override
    public SiteResponse enableSSL(Long id, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setSslEnabled(true);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse disableSSL(Long id, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setSslEnabled(false);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public List<SiteResponse> getSSLEnabledSites(Language displayLanguage) {
        return siteRepository.findBySslEnabledTrue()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getSSLDisabledSites(Language displayLanguage) {
        return siteRepository.findBySslEnabledFalse()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public SiteResponse updateSEOSettings(Long id, String siteTitle, String siteDescription,
            String siteKeywords, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setSiteTitle(siteTitle);
        site.setSiteDescription(siteDescription);
        site.setSiteKeywords(siteKeywords);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse updateOGSettings(Long id, String ogImageUrl, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setOgImageUrl(ogImageUrl);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse updateSocialSettings(Long id, String twitterHandle, String facebookPageUrl,
            Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setTwitterHandle(twitterHandle);
        site.setFacebookPageUrl(facebookPageUrl);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public SiteResponse updateAnalyticsSettings(Long id, String googleAnalyticsId,
            String googleTagManagerId, Language displayLanguage) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        site.setGoogleAnalyticsId(googleAnalyticsId);
        site.setGoogleTagManagerId(googleTagManagerId);
        site.setUpdatedAt(LocalDateTime.now());

        Site updatedSite = siteRepository.save(site);
        return siteMapper.toResponse(updatedSite, displayLanguage);
    }

    @Override
    public List<SiteResponse> getSitesWithAnalytics(Language displayLanguage) {
        return siteRepository.findWithAnalytics()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> searchSitesByName(String searchTerm, Language displayLanguage) {
        return siteRepository.findBySiteNameContainingIgnoreCase(searchTerm)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> searchSitesByDescription(String searchTerm, Language displayLanguage) {
        return siteRepository.findByDescriptionContainingIgnoreCase(searchTerm)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getSitesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Language displayLanguage) {
        return siteRepository.findByCreatedAtBetween(startDate, endDate)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getSitesPublishedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Language displayLanguage) {
        return siteRepository.findByPublishedAtBetween(startDate, endDate)
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteResponse> getRecentlyPublishedSites(Language displayLanguage) {
        return siteRepository.findRecentlyPublishedSites()
                .stream()
                .map(site -> siteMapper.toResponse(site, displayLanguage))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canSiteBePublished(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        return site.canBePublished();
    }

    @Override
    public boolean isSiteAccessible(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + id));

        return site.isAccessible();
    }

    @Override
    public long getTotalSitesCount() {
        return siteRepository.count();
    }

    @Override
    public long getPublishedSitesCount() {
        return siteRepository.findByPublishedTrue().size();
    }

    @Override
    public long getMaintenanceSitesCount() {
        return siteRepository.findByMaintenanceModeTrue().size();
    }

    @Override
    public long getSSLEnabledSitesCount() {
        return siteRepository.findBySslEnabledTrue().size();
    }

    @Override
    public void bulkPublish(List<Long> siteIds) {
        List<Site> sites = siteRepository.findByIdIn(siteIds);
        for (Site site : sites) {
            site.setPublished(true);
            site.setPublishedAt(LocalDateTime.now());
            site.setUpdatedAt(LocalDateTime.now());
        }
        siteRepository.saveAll(sites);
    }

    @Override
    public void bulkUnpublish(List<Long> siteIds) {
        List<Site> sites = siteRepository.findByIdIn(siteIds);
        for (Site site : sites) {
            site.setPublished(false);
            site.setPublishedAt(null);
            site.setUpdatedAt(LocalDateTime.now());
        }
        siteRepository.saveAll(sites);
    }

    @Override
    public void bulkEnableSSL(List<Long> siteIds) {
        List<Site> sites = siteRepository.findByIdIn(siteIds);
        for (Site site : sites) {
            site.setSslEnabled(true);
            site.setUpdatedAt(LocalDateTime.now());
        }
        siteRepository.saveAll(sites);
    }

    @Override
    public void bulkDisableSSL(List<Long> siteIds) {
        List<Site> sites = siteRepository.findByIdIn(siteIds);
        for (Site site : sites) {
            site.setSslEnabled(false);
            site.setUpdatedAt(LocalDateTime.now());
        }
        siteRepository.saveAll(sites);
    }

    @Override
    public void bulkDelete(List<Long> siteIds) {
        siteRepository.deleteAllById(siteIds);
    }
}
