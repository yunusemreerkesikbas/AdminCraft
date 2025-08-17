package com.backend.presentation.controller;

import com.backend.application.service.SiteService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreateSiteRequest;
import com.backend.presentation.dto.request.UpdateSiteRequest;
import com.backend.presentation.dto.response.SiteResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
@Slf4j
public class SiteController {

    private final SiteService siteService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @Valid @RequestBody CreateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.createSite(request, displayLanguage);
            String message = messageSource.getMessage("site.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error creating site: {}", ex.getMessage());
            String message = messageSource.getMessage("site.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Optional<SiteResponse> response = siteService.getSiteById(id, displayLanguage);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                String message = messageSource.getMessage("site.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting site by id {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSitesByTenant(
            @PathVariable @Valid @NotNull @Min(1) Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            List<SiteResponse> sites = siteService.getSitesByTenantId(tenantId, displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting sites by tenant {}: {}", tenantId, ex.getMessage());
            String message = messageSource.getMessage("site.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAllSites(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            List<SiteResponse> sites = siteService.getAllSites(displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting all sites: {}", ex.getMessage());
            String message = messageSource.getMessage("site.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> updateSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @Valid @RequestBody UpdateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.updateSite(id, request, displayLanguage);
            String message = messageSource.getMessage("site.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error updating site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            siteService.deleteSite(id);
            String message = messageSource.getMessage("site.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SiteResponse>> publishSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.publishSite(id, displayLanguage);
            String message = messageSource.getMessage("site.published.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error publishing site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.publish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<SiteResponse>> unpublishSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.unpublishSite(id, displayLanguage);
            String message = messageSource.getMessage("site.unpublished.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error unpublishing site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.unpublish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<SiteResponse>> enableMaintenanceMode(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestParam(required = false) String message,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.enableMaintenanceMode(id, message, displayLanguage);
            String successMessage = messageSource.getMessage("site.maintenance.enabled.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error enabling maintenance mode for site {}: {}", id, ex.getMessage());
            String errorMessage = messageSource.getMessage("site.maintenance.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
        }
    }

    @DeleteMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<SiteResponse>> disableMaintenanceMode(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.disableMaintenanceMode(id, displayLanguage);
            String message = messageSource.getMessage("site.maintenance.disabled.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error disabling maintenance mode for site {}: {}", id, ex.getMessage());
            String errorMessage = messageSource.getMessage("site.maintenance.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteByDomain(
            @PathVariable @Valid String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize domain input
            String sanitizedDomain = sanitizeInput(domain);
            if (sanitizedDomain == null || sanitizedDomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid domain");
            }
            
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Optional<SiteResponse> response = siteService.getSiteByDomain(sanitizedDomain, displayLanguage);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                String message = messageSource.getMessage("site.domain.not.found", new Object[]{domain}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting site by domain {}: {}", domain, ex.getMessage());
            String message = messageSource.getMessage("site.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/domain/{domain}")
    public ResponseEntity<ApiResponse<Boolean>> checkDomainAvailability(
            @PathVariable @Valid String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize domain input
            String sanitizedDomain = sanitizeInput(domain);
            if (sanitizedDomain == null || sanitizedDomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid domain");
            }
            
            boolean available = siteService.isDomainAvailable(sanitizedDomain);
            String messageKey = available ? "site.domain.available" : "site.domain.taken";
            String message = messageSource.getMessage(messageKey, new Object[]{domain}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            log.error("Error checking domain availability for {}: {}", domain, ex.getMessage());
            String message = messageSource.getMessage("site.domain.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Sanitizes input to prevent XSS and other injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Basic input sanitization
        return input.trim()
                .replaceAll("[<>\"'&]", "")
                .toLowerCase();
    }
}