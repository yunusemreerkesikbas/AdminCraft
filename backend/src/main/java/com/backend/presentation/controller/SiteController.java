package com.backend.presentation.controller;

import com.backend.domain.entity.Site;
import com.backend.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
@Slf4j
public class SiteController {

    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Site>> getSiteById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // TODO: Implement site service when ready
            String message = messageSource.getMessage("site.not.implemented", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting site by id", ex);
            String message = messageSource.getMessage("site.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Site>>> getSitesByTenant(
            @PathVariable Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // TODO: Implement site service when ready
            List<Site> sites = new ArrayList<>();
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting sites by tenant", ex);
            String message = messageSource.getMessage("site.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Site>>> getAllSites(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // TODO: Implement site service when ready
            List<Site> sites = new ArrayList<>();
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting all sites", ex);
            String message = messageSource.getMessage("site.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // TODO: Implement site service when ready
            String message = messageSource.getMessage("site.delete.not.implemented", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error deleting site", ex);
            String message = messageSource.getMessage("site.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }
}