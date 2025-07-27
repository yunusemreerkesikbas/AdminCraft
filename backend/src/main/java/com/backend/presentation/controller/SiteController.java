package com.backend.presentation.controller;

import com.backend.application.service.SiteService;
import com.backend.application.service.TenantService;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.mapper.SiteMapper;
import com.backend.presentation.dto.request.CreateSiteRequest;
import com.backend.presentation.dto.request.UpdateSiteRequest;
import com.backend.presentation.dto.response.SiteResponse;
import com.backend.presentation.dto.response.MenuResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @Valid @RequestBody CreateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            // Convert DTO to Entity
            Site site = siteMapper.toEntity(request);
            
            // Set tenant ID from context (would normally come from JWT token)
            site.setTenantId(1L); // TODO: Get from security context
            
            // Create site
            Site savedSite = siteService.createSite(site);
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(savedSite.getTenantId());
            
            SiteResponse response = siteMapper.toResponse(savedSite, tenant.orElse(null), null);
            
            String message = messageSource.getMessage("site.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Optional<Site> siteOpt = siteService.getSiteById(id);
            if (siteOpt.isEmpty()) {
                String message = messageSource.getMessage("site.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            Site site = siteOpt.get();
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(site.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(site.getId());
            
            SiteResponse response = siteMapper.toResponse(site, tenant.orElse(null), menus);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAllSites(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean published,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Long tenantId = 1L; // TODO: Get from security context
            
            List<Site> sites;
            if (active != null && active) {
                sites = siteService.getActiveSites(tenantId);
            } else if (published != null && published) {
                sites = siteService.getPublishedSites(tenantId);
            } else {
                sites = siteService.getSitesByTenantId(tenantId);
            }
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(tenantId);
            
            List<SiteResponse> responses = sites.stream()
                .map(site -> {
                    List<Menu> menus = siteService.getMenusBySiteId(site.getId());
                    return siteMapper.toResponse(site, tenant.orElse(null), menus);
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Optional<Site> existingSiteOpt = siteService.getSiteById(id);
            if (existingSiteOpt.isEmpty()) {
                String message = messageSource.getMessage("site.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            Site existingSite = existingSiteOpt.get();
            Site updatedSite = siteMapper.toEntity(request, existingSite);
            
            Site savedSite = siteService.updateSite(updatedSite);
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(savedSite.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(savedSite.getId());
            
            SiteResponse response = siteMapper.toResponse(savedSite, tenant.orElse(null), menus);
            
            String message = messageSource.getMessage("site.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            siteService.deleteSite(id);
            String message = messageSource.getMessage("site.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SiteResponse>> publishSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Site publishedSite = siteService.publishSite(id);
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(publishedSite.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(publishedSite.getId());
            
            SiteResponse response = siteMapper.toResponse(publishedSite, tenant.orElse(null), menus);
            
            String message = messageSource.getMessage("site.published.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.publish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<SiteResponse>> unpublishSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Site unpublishedSite = siteService.unpublishSite(id);
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(unpublishedSite.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(unpublishedSite.getId());
            
            SiteResponse response = siteMapper.toResponse(unpublishedSite, tenant.orElse(null), menus);
            
            String message = messageSource.getMessage("site.unpublished.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.unpublish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SiteResponse>> activateSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Site activatedSite = siteService.activateSite(id);
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(activatedSite.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(activatedSite.getId());
            
            SiteResponse response = siteMapper.toResponse(activatedSite, tenant.orElse(null), menus);
            
            String message = messageSource.getMessage("site.activated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.activate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<SiteResponse>> deactivateSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Site deactivatedSite = siteService.deactivateSite(id);
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(deactivatedSite.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(deactivatedSite.getId());
            
            SiteResponse response = siteMapper.toResponse(deactivatedSite, tenant.orElse(null), menus);
            
            String message = messageSource.getMessage("site.deactivated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.deactivate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusBySite(
            @PathVariable Long id,
            @RequestParam(required = false) Language language,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Menu> menus;
            if (language != null) {
                menus = siteService.getMenusBySiteIdAndLanguage(id, language);
            } else {
                menus = siteService.getMenusBySiteId(id);
            }
            
            List<MenuResponse> responses = menus.stream()
                .map(menu -> new MenuResponse(
                    menu.getId(),
                    menu.getName(),
                    menu.getLanguage(),
                    menu.getTenantId(),
                    menu.getSiteId(),
                    List.of(), // Menu items would be populated separately
                    menu.getCreatedAt(),
                    menu.getUpdatedAt()
                ))
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.menus.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteByDomain(
            @PathVariable String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Optional<Site> siteOpt = siteService.getSiteByDomain(domain);
            if (siteOpt.isEmpty()) {
                String message = messageSource.getMessage("site.domain.not.found", new Object[]{domain}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            Site site = siteOpt.get();
            
            // Get additional data for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(site.getTenantId());
            List<Menu> menus = siteService.getMenusBySiteId(site.getId());
            
            SiteResponse response = siteMapper.toResponse(site, tenant.orElse(null), menus);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.domain.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/domain/{domain}")
    public ResponseEntity<ApiResponse<Boolean>> checkDomainAvailability(
            @PathVariable String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            boolean available = siteService.isDomainAvailable(domain);
            String messageKey = available ? "site.domain.available" : "site.domain.taken";
            String message = messageSource.getMessage(messageKey, new Object[]{domain}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            String message = messageSource.getMessage("site.domain.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}