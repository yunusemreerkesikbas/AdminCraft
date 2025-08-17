package com.backend.presentation.controller;

import com.backend.application.service.ContentService;
import com.backend.domain.entity.Content;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContentController {

    private final ContentService contentService;
    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Content>> getContentById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Content> content = contentService.getContentById(id);
            if (content.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(content.get()));
            } else {
                String message = messageSource.getMessage("content.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting content by id {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("content.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Content>> getContentBySlug(
            @PathVariable @Valid @NotBlank String slug,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize slug input
            String sanitizedSlug = sanitizeInput(slug);
            if (sanitizedSlug == null || sanitizedSlug.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid slug");
            }
            
            // TODO: Implement getContentBySlug in ContentService when ready
            String message = messageSource.getMessage("content.slug.not.implemented", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting content by slug {}: {}", slug, ex.getMessage());
            String message = messageSource.getMessage("content.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Content>>> getContentByTenant(
            @PathVariable @Valid @NotNull @Min(1) Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Content> content = contentService.getContentByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(content));
        } catch (Exception ex) {
            log.error("Error getting content by tenant {}: {}", tenantId, ex.getMessage());
            String message = messageSource.getMessage("content.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Content>>> getAllContent(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Content> content = contentService.getAllContent();
            return ResponseEntity.ok(ApiResponse.success(content));
        } catch (Exception ex) {
            log.error("Error getting all content: {}", ex.getMessage());
            String message = messageSource.getMessage("content.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            
            contentService.deleteContent(id);
            String message = messageSource.getMessage("content.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting content {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("content.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Content>> publishContent(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            
            // Get current user ID for publishing
            Long currentUserId = getCurrentUserId();
            Content publishedContent = contentService.publishContent(id, currentUserId);
            String message = messageSource.getMessage("content.publish.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, publishedContent));
        } catch (Exception ex) {
            log.error("Error publishing content {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("content.publish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<Content>> unpublishContent(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            
            // Get current user ID for unpublishing
            Long currentUserId = getCurrentUserId();
            Content unpublishedContent = contentService.unpublishContent(id, currentUserId);
            String message = messageSource.getMessage("content.unpublish.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, unpublishedContent));
        } catch (Exception ex) {
            log.error("Error unpublishing content {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("content.unpublish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Gets the current user ID from the security context
     */
    private Long getCurrentUserId() {
        try {
            // This is a simplified version - in a real implementation you would
            // get the user ID from the JWT token or security context
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            // TODO: Implement a service method to get user ID by email
            return 1L; // Placeholder - replace with actual implementation
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to get current user ID", ex);
        }
    }

    /**
     * Validates tenant access based on current user context
     */
    private void validateTenantAccess(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
        
        // TODO: Implement actual tenant access validation based on current user's tenant
        // In a real implementation, you would check if the current user has access to this tenant
        // Example:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // UserDetails userDetails = (UserDetails) auth.getPrincipal();
        // if (!tenantService.userHasAccessToTenant(userDetails.getUsername(), tenantId)) {
        //     throw new AccessDeniedException("Access denied to tenant: " + tenantId);
        // }
    }

    /**
     * Sanitizes input to prevent XSS and other injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Basic XSS prevention - remove potentially dangerous characters
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
}