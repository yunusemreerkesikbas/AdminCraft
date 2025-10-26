package com.backend.presentation.controller;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.MediaFile;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MediaController {

    private final MediaService mediaService;
    private final MessageSource messageSource;

    // Security constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "text/plain",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "video/mp4", "audio/mpeg", "audio/mp3"
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFile>> uploadFile(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("tenantId") @NotNull @Min(1) Long tenantId,
            @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Security validations
            validateFileUpload(file);
            validateTenantAccess(tenantId);
            
            MediaFile mediaFile = mediaService.uploadFile(file, tenantId, uploadedBy);
            String message = messageSource.getMessage("media.upload.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, mediaFile));
        } catch (Exception ex) {
            log.error("Error uploading file for tenant {} by user {}: {}", tenantId, uploadedBy, ex.getMessage());
            String message = messageSource.getMessage("media.upload.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaFile>> getMediaFile(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                // Access control handled by TenantContext routing
                return ResponseEntity.ok(ApiResponse.success(mediaFile.get()));
            } else {
                String message = messageSource.getMessage("media.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<MediaFile>>> getMediaFilesByTenant(
            @PathVariable @Valid @NotNull @Min(1) Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            validateTenantAccess(tenantId);
            List<MediaFile> mediaFiles = mediaService.getMediaFilesByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting media files for tenant {}: {}", tenantId, ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMediaFile(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Access control handled by TenantContext routing
            mediaService.deleteMediaFile(id);
            String message = messageSource.getMessage("media.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaFile>>> getAllMediaFiles(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<MediaFile> mediaFiles = mediaService.getAllMediaFiles();
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting all media files: {}", ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}/alt-text")
    public ResponseEntity<ApiResponse<MediaFile>> updateAltText(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestParam @Valid String altText,
            @RequestParam(defaultValue = "tr") String language,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Validate alt text input
            if (altText != null && altText.length() > 255) {
                throw new IllegalArgumentException("Alt text cannot exceed 255 characters");
            }
            
            // Access control handled by TenantContext routing

            // Sanitize alt text
            String sanitizedAltText = sanitizeInput(altText);
            
            MediaFile updatedFile = mediaService.updateAltText(id, 
                com.backend.domain.enums.Language.fromCodeOrDefault(language), sanitizedAltText);
            String message = messageSource.getMessage("media.alt.text.updated", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, updatedFile));
        } catch (Exception ex) {
            log.error("Error updating alt text for media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Validates file upload security requirements
     */
    private void validateFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        // File size validation
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        // Content type validation
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
        
        // File name validation
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
        
        // Check for potentially dangerous file extensions
        String filename = originalFilename.toLowerCase();
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\") ||
            filename.endsWith(".exe") || filename.endsWith(".bat") || filename.endsWith(".cmd") ||
            filename.endsWith(".scr") || filename.endsWith(".js") || filename.endsWith(".vbs")) {
            throw new IllegalArgumentException("Filename contains invalid characters or dangerous extension");
        }
    }

    /**
     * Validates tenant access based on current user context
     * This should be implemented based on your security requirements
     */
    private void validateTenantAccess(Long tenantId) {
        // TODO: Implement actual tenant access validation based on current user's tenant
        // For now, just validate that tenantId is not null and positive
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
        
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