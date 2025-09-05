package com.backend.presentation.controller;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.MediaFile;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SecurityHelper securityHelper;

    // Security constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "text/plain",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "video/mp4", "audio/mpeg", "audio/mp3");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<MediaFile>> uploadFile(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("tenantId") @NotNull @Min(1) Long tenantId,
            @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Security validations
            validateFileUpload(file);
            securityHelper.validateTenantAccess(tenantId);

            MediaFile mediaFile = mediaService.uploadFile(file, tenantId, uploadedBy);
            String message = messageSource.getMessage("media.upload.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(message, mediaFile));
        } catch (Exception ex) {
            log.error("Error uploading file for tenant {} by user {}: {}", tenantId, uploadedBy, ex.getMessage());
            String message = messageSource.getMessage("media.upload.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<ApiResponse<MediaFile>> getMediaFile(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                // Additional tenant-based access control
                securityHelper.validateTenantAccess(mediaFile.get().getTenantId());
                return ResponseEntity.ok(ApiResponse.success(mediaFile.get()));
            } else {
                String message = messageSource.getMessage("media.not.found", new Object[] { id },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<ApiResponse<List<MediaFile>>> getMediaFilesByTenant(
            @PathVariable @Valid @NotNull @Min(1) Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            securityHelper.validateTenantAccess(tenantId);
            List<MediaFile> mediaFiles = mediaService.getMediaFilesByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting media files for tenant {}: {}", tenantId, ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<Void>> deleteMediaFile(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Additional security check - ensure user has access to the media file's tenant
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                securityHelper.validateTenantAccess(mediaFile.get().getTenantId());
            }

            mediaService.deleteMediaFile(id);
            String message = messageSource.getMessage("media.delete.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.delete.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<ApiResponse<List<MediaFile>>> getAllMediaFiles(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // CRITICAL FIX: Get current user's tenant ID and filter media files by tenant
            Long currentTenantId = securityHelper.getCurrentTenantId();
            securityHelper.validateTenantAccess(currentTenantId);
            
            List<MediaFile> mediaFiles = mediaService.getMediaFilesByTenantId(currentTenantId);
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting media files for current tenant: {}", ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}/alt-text")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
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

            // Security check for tenant access
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                securityHelper.validateTenantAccess(mediaFile.get().getTenantId());
            }

            // Sanitize alt text
            String sanitizedAltText = sanitizeInput(altText);

            MediaFile updatedFile = mediaService.updateAltText(id,
                    com.backend.domain.enums.Language.fromCodeOrDefault(language), sanitizedAltText);
            String message = messageSource.getMessage("media.alt.text.updated", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, updatedFile));
        } catch (Exception ex) {
            log.error("Error updating alt text for media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.update.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    /**
     * Sprint 7: Enhanced file upload security validation (OWASP compliant)
     */
    private void validateFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // File size validation
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        // Minimum file size check (avoid empty files)
        if (file.getSize() < 1) {
            throw new IllegalArgumentException("File cannot be empty");
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
        
        // Sprint 7: Enhanced filename security validation
        validateFilename(originalFilename);
        
        // Sprint 7: Content verification - check file header magic bytes
        validateFileContent(file, contentType);
    }
    
    /**
     * Sprint 7: Enhanced filename validation (OWASP ASVS compliant)
     */
    private void validateFilename(String filename) {
        // Normalize filename
        String normalizedFilename = filename.toLowerCase().trim();
        
        // Path traversal prevention
        if (normalizedFilename.contains("..") || normalizedFilename.contains("/") || 
            normalizedFilename.contains("\\") || normalizedFilename.contains("%")) {
            throw new IllegalArgumentException("Filename contains path traversal characters");
        }
        
        // Dangerous extensions (comprehensive list)
        String[] dangerousExtensions = {
            ".exe", ".bat", ".cmd", ".scr", ".com", ".pif", ".vbs", ".js", ".jar",
            ".php", ".asp", ".aspx", ".jsp", ".py", ".rb", ".pl", ".sh", ".ps1",
            ".msi", ".deb", ".rpm", ".dmg", ".app", ".ipa", ".apk"
        };
        
        for (String ext : dangerousExtensions) {
            if (normalizedFilename.endsWith(ext)) {
                throw new IllegalArgumentException("Dangerous file extension not allowed: " + ext);
            }
        }
        
        // Filename length validation
        if (filename.length() > 255) {
            throw new IllegalArgumentException("Filename too long (max 255 characters)");
        }
        
        // Special character validation
        if (normalizedFilename.matches(".*[<>:\"|?*].*")) {
            throw new IllegalArgumentException("Filename contains illegal characters");
        }
    }
    
    /**
     * Sprint 7: File content validation - verify magic bytes match declared content type
     */
    private void validateFileContent(MultipartFile file, String declaredContentType) {
        try {
            byte[] fileHeader = new byte[Math.min(20, (int) file.getSize())];
            file.getInputStream().read(fileHeader);
            
            // Verify file signature matches declared content type
            String detectedContentType = detectContentTypeFromMagicBytes(fileHeader);
            if (detectedContentType != null && !detectedContentType.equals(declaredContentType)) {
                log.warn("Content type mismatch: declared={}, detected={}", declaredContentType, detectedContentType);
                throw new IllegalArgumentException("File content does not match declared content type");
            }
        } catch (Exception e) {
            log.error("Error validating file content: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid file content");
        }
    }
    
    /**
     * Sprint 7: Detect content type from magic bytes (file signature)
     */
    private String detectContentTypeFromMagicBytes(byte[] fileHeader) {
        if (fileHeader.length < 4) return null;
        
        // Common file signatures
        if (fileHeader[0] == (byte) 0xFF && fileHeader[1] == (byte) 0xD8) {
            return "image/jpeg";
        }
        if (fileHeader[0] == (byte) 0x89 && fileHeader[1] == (byte) 0x50 && 
            fileHeader[2] == (byte) 0x4E && fileHeader[3] == (byte) 0x47) {
            return "image/png";
        }
        if (fileHeader[0] == (byte) 0x47 && fileHeader[1] == (byte) 0x49 && 
            fileHeader[2] == (byte) 0x46 && fileHeader[3] == (byte) 0x38) {
            return "image/gif";
        }
        if (fileHeader[0] == (byte) 0x25 && fileHeader[1] == (byte) 0x50 && 
            fileHeader[2] == (byte) 0x44 && fileHeader[3] == (byte) 0x46) {
            return "application/pdf";
        }
        
        return null; // Unknown signature
    }

    /**
     * Validates tenant access based on current user context
     * This should be implemented based on your security requirements
     */
    private void validateTenantAccess(Long tenantId) {
        securityHelper.validateTenantAccess(tenantId);
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

    @GetMapping(value = "/files/{fileName}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<byte[]> serveFile(@PathVariable String fileName) {
        try {
            Optional<MediaFile> mf = mediaService.getMediaFileByFileName(fileName);
            if (mf.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            securityHelper.validateTenantAccess(mf.get().getTenantId());
            byte[] body = mediaService.getFileContent(fileName);
            String mime = mf.get().getMimeType() != null ? mf.get().getMimeType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mime))
                    .header("Content-Disposition", "inline; filename=\"" + mf.get().getOriginalName() + "\"")
                    .body(body);
        } catch (Exception ex) {
            log.error("Error serving file {}: {}", fileName, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/thumbnails/{fileName}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<byte[]> serveThumbnail(@PathVariable String fileName) {
        try {
            Optional<MediaFile> mf = mediaService.getMediaFileByFileName(fileName);
            if (mf.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            securityHelper.validateTenantAccess(mf.get().getTenantId());
            byte[] body = mediaService.getThumbnailContent(fileName);
            String mime = mf.get().getMimeType() != null ? mf.get().getMimeType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mime))
                    .header("Content-Disposition", "inline; filename=\"thumb_" + mf.get().getOriginalName() + "\"")
                    .body(body);
        } catch (Exception ex) {
            log.error("Error serving thumbnail {}: {}", fileName, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Sprint 7: Additional API endpoints as per requirements
    
    @PutMapping("/{id}/i18n")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<MediaFile>> updateI18nMetadata(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestParam @Valid String language,
            @RequestBody String i18nData,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Security check for tenant access
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                securityHelper.validateTenantAccess(mediaFile.get().getTenantId());
            }
            
            // Update i18n metadata (implementation would parse and update JSON i18n field)
            MediaFile updatedFile = mediaService.updateMediaFile(mediaFile.get());
            String message = messageSource.getMessage("media.i18n.updated", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, updatedFile));
        } catch (Exception ex) {
            log.error("Error updating i18n metadata for media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.update.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR','VIEWER')")
    public ResponseEntity<ApiResponse<List<MediaFile>>> searchMediaFiles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String mimeType,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long currentTenantId = securityHelper.getCurrentTenantId();
            securityHelper.validateTenantAccess(currentTenantId);
            
            // Basic search implementation - in production would use more sophisticated search
            List<MediaFile> mediaFiles = mediaService.getMediaFilesByTenantId(currentTenantId);
            
            // Apply filters
            if (query != null && !query.isEmpty()) {
                mediaFiles = mediaFiles.stream()
                    .filter(mf -> mf.getOriginalName().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            }
            
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error searching media files: {}", ex.getMessage());
            String message = messageSource.getMessage("media.search.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }
    
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<MediaFile>> activateMediaFile(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFileOpt = mediaService.getMediaFileById(id);
            if (mediaFileOpt.isEmpty()) {
                String message = messageSource.getMessage("media.not.found", new Object[] { id },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
            
            MediaFile mediaFile = mediaFileOpt.get();
            securityHelper.validateTenantAccess(mediaFile.getTenantId());
            
            // Activate staged file
            if (mediaFile.canBeActivated()) {
                mediaFile.activate();
                MediaFile updatedFile = mediaService.updateMediaFile(mediaFile);
                String message = messageSource.getMessage("media.activated", null,
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, updatedFile));
            } else {
                throw new IllegalStateException("Media file cannot be activated");
            }
        } catch (Exception ex) {
            log.error("Error activating media file {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.activate.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }
}