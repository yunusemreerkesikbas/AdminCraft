package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.Site;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.Menu;
import com.backend.presentation.dto.request.CreateSiteRequest;
import com.backend.presentation.dto.request.UpdateSiteRequest;
import com.backend.presentation.dto.response.SiteResponse;
import com.backend.presentation.dto.response.MenuResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class SiteMapper {
    
    public Site toEntity(CreateSiteRequest request) {
        Site site = new Site();
        site.setSiteName(request.siteName());
        site.setDescription(request.description());
        site.setDefaultLanguage(request.defaultLanguage());
        site.setEnabledLanguages(request.enabledLanguages() != null ? request.enabledLanguages() : Set.of(request.defaultLanguage()));
        site.setDomain(request.domain());
        site.setTheme(request.theme() != null ? request.theme() : "default");
        site.setLogoUrl(request.logoUrl());
        site.setFaviconUrl(request.faviconUrl());
        site.setPrimaryColor(request.primaryColor());
        site.setSecondaryColor(request.secondaryColor());
        site.setFontFamily(request.fontFamily());
        site.setMetaTitle(request.metaTitle());
        site.setMetaDescription(request.metaDescription());
        site.setMetaKeywords(request.metaKeywords());
        site.setGoogleAnalyticsId(request.googleAnalyticsId());
        site.setCustomCode(request.customCode());
        site.setIsActive(request.isActive() != null ? request.isActive() : true);
        
        // Set defaults
        site.setIsPublished(false);
        site.setCreatedAt(LocalDateTime.now());
        site.setUpdatedAt(LocalDateTime.now());
        
        return site;
    }
    
    public Site toEntity(UpdateSiteRequest request, Site existingSite) {
        existingSite.setSiteName(request.siteName());
        existingSite.setDescription(request.description());
        existingSite.setDefaultLanguage(request.defaultLanguage());
        existingSite.setEnabledLanguages(request.enabledLanguages() != null ? request.enabledLanguages() : Set.of(request.defaultLanguage()));
        existingSite.setDomain(request.domain());
        existingSite.setTheme(request.theme() != null ? request.theme() : "default");
        existingSite.setLogoUrl(request.logoUrl());
        existingSite.setFaviconUrl(request.faviconUrl());
        existingSite.setPrimaryColor(request.primaryColor());
        existingSite.setSecondaryColor(request.secondaryColor());
        existingSite.setFontFamily(request.fontFamily());
        existingSite.setMetaTitle(request.metaTitle());
        existingSite.setMetaDescription(request.metaDescription());
        existingSite.setMetaKeywords(request.metaKeywords());
        existingSite.setGoogleAnalyticsId(request.googleAnalyticsId());
        existingSite.setCustomCode(request.customCode());
        existingSite.setIsActive(request.isActive() != null ? request.isActive() : true);
        existingSite.setUpdatedAt(LocalDateTime.now());
        
        return existingSite;
    }
    
    public SiteResponse toResponse(Site site) {
        return toResponse(site, null, null);
    }
    
    public SiteResponse toResponse(Site site, Tenant tenant, List<Menu> menus) {
        return new SiteResponse(
            site.getId(),
            site.getSiteName(),
            site.getDescription(),
            site.getEnabledLanguages(),
            site.getDefaultLanguage(),
            site.getTenantId(),
            tenant != null ? tenant.getCompanyName() : null,
            site.getDomain(),
            site.getIsActive(),
            site.getTheme(),
            site.getLogoUrl(),
            site.getFaviconUrl(),
            site.getPrimaryColor(),
            site.getSecondaryColor(),
            site.getFontFamily(),
            site.getMetaTitle(),
            site.getMetaDescription(),
            site.getMetaKeywords(),
            site.getGoogleAnalyticsId(),
            site.getCustomCode(),
            site.getIsPublished(),
            menus != null ? menus.stream().map(this::toMenuResponse).map(Object.class::cast).toList() : List.of(),
            site.getCreatedAt(),
            site.getUpdatedAt(),
            site.getPublishedAt()
        );
    }
    
    private MenuResponse toMenuResponse(Menu menu) {
        return new MenuResponse(
            menu.getId(),
            menu.getName(),
            menu.getLanguage(),
            menu.getTenantId(),
            menu.getSiteId(),
            List.of(), // Menu items would be populated separately
            menu.getCreatedAt(),
            menu.getUpdatedAt()
        );
    }
}