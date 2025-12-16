package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.application.dto.request.CreateSiteRequest;
import com.backend.application.dto.request.UpdateSiteRequest;
import com.backend.presentation.dto.response.SiteResponse;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {
    
    public SiteResponse toResponse(Site site, Language displayLanguage) {
        if (site == null) {
            return null;
        }
        
        return new SiteResponse(
            site.getId(),
            site.getSiteName(),
            site.getDescription(),
            null, // tenantName will be populated separately if needed
            site.getDomain(),
            site.getCustomDomain(),
            site.getEnabledLanguages(),
            site.getDefaultLanguage(),
            site.getSslEnabled(),
            site.getPublished(),
            site.getThemeName(),
            site.getMaintenanceMode(),
            site.getMaintenanceMessage(),
            site.getSiteTitle(),
            site.getSiteDescription(),
            site.getSiteKeywords(),
            site.getOgImageUrl(),
            site.getTwitterHandle(),
            site.getFacebookPageUrl(),
            site.getGoogleAnalyticsId(),
            site.getGoogleTagManagerId(),
            site.getSiteUrl(),
            site.getFullDomain(),
            site.isAccessible(),
            site.canBePublished(),
            site.getCreatedAt(),
            site.getUpdatedAt(),
            site.getPublishedAt()
        );
    }
    
    public Site toEntity(SiteResponse response) {
        if (response == null) {
            return null;
        }
        
        Site site = new Site();
        site.setId(response.id());
        site.setSiteName(response.siteName());
        site.setDescription(response.description());
        site.setDomain(response.domain());
        site.setCustomDomain(response.customDomain());
        site.setEnabledLanguages(response.enabledLanguages());
        site.setDefaultLanguage(response.defaultLanguage());
        site.setSslEnabled(response.sslEnabled());
        site.setPublished(response.published());
        site.setThemeName(response.themeName());
        site.setMaintenanceMode(response.maintenanceMode());
        site.setMaintenanceMessage(response.maintenanceMessage());
        site.setSiteTitle(response.siteTitle());
        site.setSiteDescription(response.siteDescription());
        site.setSiteKeywords(response.siteKeywords());
        site.setOgImageUrl(response.ogImageUrl());
        site.setTwitterHandle(response.twitterHandle());
        site.setFacebookPageUrl(response.facebookPageUrl());
        site.setGoogleAnalyticsId(response.googleAnalyticsId());
        site.setGoogleTagManagerId(response.googleTagManagerId());

        return site;
    }
    
    public Site toEntity(CreateSiteRequest request) {
        if (request == null) {
            return null;
        }
        
        Site site = new Site();
        site.setSiteName(request.siteName());
        site.setDescription(request.description());
        site.setDomain(request.domain());
        site.setCustomDomain(request.customDomain());
        site.setEnabledLanguages(request.enabledLanguages());
        site.setDefaultLanguage(request.defaultLanguage());
        site.setSslEnabled(request.sslEnabled() != null ? request.sslEnabled() : true);
        site.setPublished(false); // New sites start unpublished
        site.setThemeName(request.themeName() != null ? request.themeName() : "default");
        site.setMaintenanceMode(false);
        site.setMaintenanceMessage(null);
        site.setSiteTitle(request.siteTitle());
        site.setSiteDescription(request.siteDescription());
        site.setSiteKeywords(request.siteKeywords());
        site.setOgImageUrl(request.ogImageUrl());
        site.setTwitterHandle(request.twitterHandle());
        site.setFacebookPageUrl(request.facebookPageUrl());
        site.setGoogleAnalyticsId(request.googleAnalyticsId());
        site.setGoogleTagManagerId(request.googleTagManagerId());

        return site;
    }
    
    public void updateEntity(Site site, UpdateSiteRequest request) {
        if (site == null || request == null) {
            return;
        }
        
        // Only update non-null fields
        if (request.siteName() != null) {
            site.setSiteName(request.siteName());
        }
        if (request.description() != null) {
            site.setDescription(request.description());
        }
        if (request.domain() != null) {
            site.setDomain(request.domain());
        }
        if (request.customDomain() != null) {
            site.setCustomDomain(request.customDomain());
        }
        if (request.defaultLanguage() != null) {
            site.setDefaultLanguage(request.defaultLanguage());
        }
        if (request.enabledLanguages() != null) {
            site.setEnabledLanguages(request.enabledLanguages());
        }
        if (request.themeName() != null) {
            site.setThemeName(request.themeName());
        }
        if (request.sslEnabled() != null) {
            site.setSslEnabled(request.sslEnabled());
        }
        if (request.published() != null) {
            site.setPublished(request.published());
        }
        if (request.maintenanceMode() != null) {
            site.setMaintenanceMode(request.maintenanceMode());
        }
        if (request.maintenanceMessage() != null) {
            site.setMaintenanceMessage(request.maintenanceMessage());
        }
        if (request.siteTitle() != null) {
            site.setSiteTitle(request.siteTitle());
        }
        if (request.siteDescription() != null) {
            site.setSiteDescription(request.siteDescription());
        }
        if (request.siteKeywords() != null) {
            site.setSiteKeywords(request.siteKeywords());
        }
        if (request.ogImageUrl() != null) {
            site.setOgImageUrl(request.ogImageUrl());
        }
        if (request.twitterHandle() != null) {
            site.setTwitterHandle(request.twitterHandle());
        }
        if (request.facebookPageUrl() != null) {
            site.setFacebookPageUrl(request.facebookPageUrl());
        }
        if (request.googleAnalyticsId() != null) {
            site.setGoogleAnalyticsId(request.googleAnalyticsId());
        }
        if (request.googleTagManagerId() != null) {
            site.setGoogleTagManagerId(request.googleTagManagerId());
        }
    }
}
