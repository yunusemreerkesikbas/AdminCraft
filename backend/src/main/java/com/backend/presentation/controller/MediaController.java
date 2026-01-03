package com.backend.presentation.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.service.MediaContainerService;
import com.backend.application.service.MediaI18nService;
import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaContainer;
import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.MediaI18nRequest;
import com.backend.presentation.dto.request.MediaUpdateRequest;
import com.backend.presentation.dto.response.MediaDetailResponse;
import com.backend.presentation.dto.response.MediaI18nResponse;
import com.backend.presentation.dto.response.MediaResponse;
import com.backend.shared.common.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Media controller for managing media files.
 * Phase 1-4 implementation with CRUD, i18n, and detail operations.
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Media", description = "Endpoints for managing media files, uploads, and metadata")
public class MediaController {

        private final MediaService mediaService;
        private final MediaI18nService i18nService;
        private final MediaContainerService containerService;
        private final MessageSource messageSource;

        private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/png", "image/gif", "image/webp",
                        "application/pdf", "text/plain",
                        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "video/mp4", "audio/mpeg", "audio/mp3");

        // ========== CRUD Operations ==========

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload a media file", description = "Uploads a file, validates it, and creates a media record. Supports async processing.")
        public ResponseEntity<ApiResponse<MediaResponse>> uploadFile(
                        @Parameter(description = "The file to upload", required = true) @RequestParam("file") @Valid MultipartFile file,
                        @Parameter(description = "ID of the user uploading the file", required = true) @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
                        @Parameter(description = "Language code for messages (e.g., 'tr', 'en')", example = "tr") @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateFileUpload(file);

                        Media media = mediaService.uploadFile(file, uploadedBy);
                        String message = messageSource.getMessage("media.upload.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, MediaResponse.from(media)));
                } catch (Exception ex) {
                        log.error("Error uploading file: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.upload.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping(value = "/composite", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload media with metadata", description = "Uploads a file, assigns it to a folder, and creates i18n entries in a single transaction.")
        public ResponseEntity<ApiResponse<MediaResponse>> uploadComposite(
                        @Parameter(description = "The file to upload", required = true) @RequestParam("file") @Valid MultipartFile file,
                        @Parameter(description = "ID of the user uploading the file", required = true) @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
                        @Parameter(description = "Target folder ID") @RequestParam(value = "folderId", required = false) Long folderId,
                        @Parameter(description = "Translations JSON string") @RequestParam(value = "translations", required = false) String translationsJson,
                        @Parameter(description = "Language code for messages") @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateFileUpload(file);

                        Map<Language, MediaI18nRequest> translations = null;
                        if (translationsJson != null && !translationsJson.isEmpty()) {
                                ObjectMapper mapper = new ObjectMapper();
                                translations = mapper.readValue(translationsJson,
                                                new TypeReference<>() {
                                                });
                        }

                        Media media = mediaService.uploadComposite(file, uploadedBy, folderId, translations);

                        String message = messageSource.getMessage("media.upload.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, MediaResponse.from(media)));
                } catch (Exception ex) {
                        log.error("Error uploading composite media: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.upload.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get media by ID", description = "Retrieves media metadata by its internal ID.")
        public ResponseEntity<ApiResponse<MediaResponse>> getMediaById(
                        @Parameter(description = "Media ID", required = true) @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Optional<Media> media = mediaService.findById(id);
                        if (media.isPresent()) {
                                return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media.get())));
                        } else {
                                String message = messageSource.getMessage("media.not.found", new Object[] { id },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }
                } catch (Exception ex) {
                        log.error("Error getting media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/uid/{uid}")
        @Operation(summary = "Get media by UID", description = "Retrieves media metadata by its unique identifier (UID).")
        public ResponseEntity<ApiResponse<MediaResponse>> getMediaByUid(
                        @Parameter(description = "Media UID", required = true) @PathVariable String uid,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Optional<Media> media = mediaService.findByUid(uid);
                        if (media.isPresent()) {
                                return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media.get())));
                        } else {
                                String message = messageSource.getMessage("media.not.found", new Object[] { uid },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }
                } catch (Exception ex) {
                        log.error("Error getting media by UID {}: {}", uid, ex.getMessage());
                        String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Get all media with optional pagination.
         */
        @GetMapping
        @Operation(summary = "List all media", description = "Retrieves a paginated list of all media files.")
        public ResponseEntity<ApiResponse<Page<MediaResponse>>> getAllMedia(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                        Page<Media> mediaPage = mediaService.findAll(pageRequest);
                        Page<MediaResponse> responsePage = mediaPage.map(MediaResponse::from);
                        return ResponseEntity.ok(ApiResponse.success(responsePage));
                } catch (Exception ex) {
                        log.error("Error getting all media: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Update media metadata (folder, public flag, tags).
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update media metadata", description = "Updates media properties like folder, public status, and tags.")
        public ResponseEntity<ApiResponse<MediaResponse>> updateMedia(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid MediaUpdateRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Media media = mediaService.updateMetadata(id, request.folderId(), request.isPublic(),
                                        request.tags());
                        String message = messageSource.getMessage("media.update.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, MediaResponse.from(media)));
                } catch (IllegalArgumentException ex) {
                        log.warn("Media update validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error updating media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete media", description = "Deletes a media file and its associated data (containers, i18n, physical files).")
        public ResponseEntity<ApiResponse<Void>> deleteMedia(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        mediaService.delete(id);
                        String message = messageSource.getMessage("media.delete.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (Exception ex) {
                        log.error("Error deleting media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/folder/{folderId}")
        @Operation(summary = "Get media by folder", description = "Retrieves all media files within a specific folder.")
        public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByFolder(
                        @PathVariable @Valid @NotNull @Min(1) Long folderId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        List<Media> mediaList = mediaService.findByFolderId(folderId);
                        List<MediaResponse> responseList = mediaList.stream()
                                        .map(MediaResponse::from)
                                        .toList();
                        return ResponseEntity.ok(ApiResponse.success(responseList));
                } catch (Exception ex) {
                        log.error("Error getting media by folder {}: {}", folderId, ex.getMessage());
                        String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== File Retrieval ==========

        @GetMapping("/files/{fileName:.+}")
        @PreAuthorize("permitAll()")
        @Operation(summary = "Download media file", description = "Downloads the physical file content. Public endpoint for serving images.")
        public ResponseEntity<byte[]> downloadFile(
                        @Parameter(description = "File name (UUID)", required = true) @PathVariable("fileName") String fileName,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        byte[] content = mediaService.getFileContent(fileName);

                        Optional<Media> mediaOpt = mediaService.findByFileName(fileName);
                        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

                        if (mediaOpt.isPresent() && mediaOpt.get().getMimeType() != null) {
                                try {
                                        mediaType = MediaType.parseMediaType(mediaOpt.get().getMimeType());
                                } catch (Exception e) {
                                        log.warn("Invalid mime type for file {}: {}", fileName, e.getMessage());
                                }
                        }

                        return ResponseEntity.ok()
                                        .contentType(mediaType)
                                        .body(content);
                } catch (Exception ex) {
                        log.error("Error downloading file {}: {}", fileName, ex.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
        }

        // ========== i18n Operations ==========

        /**
         * Get i18n metadata for a media file.
         */
        @GetMapping("/{id}/i18n/{language}")
        @Operation(summary = "Get media i18n", description = "Retrieves localized metadata (title, alt text, description) for a language.")
        public ResponseEntity<ApiResponse<MediaI18nResponse>> getI18n(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Parameter(description = "Language code (TR, EN)", example = "TR") @PathVariable String language,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Language lang = Language.valueOf(language.toUpperCase());
                        Optional<MediaI18n> i18n = i18nService.get(id, lang);

                        if (i18n.isPresent()) {
                                return ResponseEntity.ok(ApiResponse.success(MediaI18nResponse.from(i18n.get())));
                        } else {
                                String message = messageSource.getMessage("media.i18n.not.found",
                                                new Object[] { id, language },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }
                } catch (IllegalArgumentException ex) {
                        log.warn("Invalid language code: {}", language);
                        String message = messageSource.getMessage("media.i18n.invalid.language",
                                        new Object[] { language },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error getting i18n for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.i18n.get.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Create or update i18n metadata for a media file.
         */
        @PutMapping("/{id}/i18n/{language}")
        @Operation(summary = "Upsert media i18n", description = "Creates or updates localized metadata for a media file.")
        public ResponseEntity<ApiResponse<MediaI18nResponse>> upsertI18n(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @PathVariable String language,
                        @RequestBody @Valid MediaI18nRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Language lang = Language.valueOf(language.toUpperCase());
                        MediaI18n i18n = i18nService.upsert(id, lang, request.altText(), request.title(),
                                        request.description());
                        String message = messageSource.getMessage("media.i18n.update.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, MediaI18nResponse.from(i18n)));
                } catch (IllegalArgumentException ex) {
                        log.warn("i18n upsert validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.i18n.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error upserting i18n for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.i18n.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Delete i18n metadata for a media file.
         */
        @DeleteMapping("/{id}/i18n/{language}")
        @Operation(summary = "Delete media i18n", description = "Removes localized metadata for a specific language.")
        public ResponseEntity<ApiResponse<Void>> deleteI18n(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @PathVariable String language,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Language lang = Language.valueOf(language.toUpperCase());
                        i18nService.delete(id, lang);
                        String message = messageSource.getMessage("media.i18n.delete.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (IllegalArgumentException ex) {
                        log.warn("i18n delete validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.i18n.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error deleting i18n for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.i18n.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Detail Operations ==========

        /**
         * Get detailed media information including container and i18n.
         */
        @GetMapping("/{id}/detail")
        @Operation(summary = "Get media details", description = "Retrieves comprehensive media information including container variants and all translations.")
        public ResponseEntity<ApiResponse<MediaDetailResponse>> getMediaDetail(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Optional<Media> mediaOpt = mediaService.findByIdWithFolder(id);
                        if (mediaOpt.isEmpty()) {
                                String message = messageSource.getMessage("media.not.found", new Object[] { id },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }

                        Media media = mediaOpt.get();

                        // Get container (with variants)
                        MediaContainer container = containerService.findByMasterMediaId(media.getId()).orElse(null);

                        // Get all i18n entries
                        List<MediaI18n> i18nList = i18nService.getByMediaId(id);

                        MediaDetailResponse detail = MediaDetailResponse.from(media, container, i18nList);
                        return ResponseEntity.ok(ApiResponse.success(detail));
                } catch (Exception ex) {
                        log.error("Error getting media detail {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Validation ==========

        private void validateFileUpload(MultipartFile file) {
                if (file == null || file.isEmpty()) {
                        throw new IllegalArgumentException("File cannot be null or empty");
                }
                if (file.getSize() > MAX_FILE_SIZE) {
                        throw new IllegalArgumentException(
                                        "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024)
                                                        + "MB");
                }
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                        throw new IllegalArgumentException("File type not allowed: " + contentType);
                }
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.trim().isEmpty()) {
                        throw new IllegalArgumentException("File must have a valid filename");
                }
                String filename = originalFilename.toLowerCase();
                if (filename.contains("..") || filename.contains("/") || filename.contains("\\") ||
                                filename.endsWith(".exe") || filename.endsWith(".bat") || filename.endsWith(".cmd") ||
                                filename.endsWith(".scr") || filename.endsWith(".js") || filename.endsWith(".vbs")) {
                        throw new IllegalArgumentException(
                                        "Filename contains invalid characters or dangerous extension");
                }
        }
}