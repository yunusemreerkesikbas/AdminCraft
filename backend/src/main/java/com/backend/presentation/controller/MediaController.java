package com.backend.presentation.controller;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
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
import com.backend.application.service.MediaProcessingService;
import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaContainer;
import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.FocalPointRequest;
import com.backend.presentation.dto.request.GenerateFormatRequest;
import com.backend.presentation.dto.request.GenerateFormatsRequest;
import com.backend.presentation.dto.request.MediaI18nRequest;
import com.backend.presentation.dto.request.MediaUpdateRequest;
import com.backend.presentation.dto.response.MediaDetailResponse;
import com.backend.presentation.dto.response.MediaI18nResponse;
import com.backend.presentation.dto.response.MediaListResponse;
import com.backend.presentation.dto.response.MediaResponse;
import com.backend.presentation.dto.response.MediaVariantResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;
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
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Media", description = "Endpoints for managing media files, uploads, and metadata")
public class MediaController {

        private final MediaService mediaService;
        private final MediaI18nService i18nService;
        private final MediaContainerService containerService;
        private final MediaProcessingService processingService;
        private final MessageSource messageSource;

        private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/png", "image/gif", "image/webp",
                        "application/pdf", "text/plain",
                        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "video/mp4", "audio/mpeg", "audio/mp3");

        // ========== CRUD Operations ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
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

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping(value = "/composite", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload media with metadata", description = "Uploads a file and creates i18n entries in a single transaction.")
        public ResponseEntity<ApiResponse<MediaResponse>> uploadComposite(
                        @Parameter(description = "The file to upload", required = true) @RequestParam("file") @Valid MultipartFile file,
                        @Parameter(description = "ID of the user uploading the file", required = true) @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
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

                        Media media = mediaService.uploadComposite(file, uploadedBy, translations);

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
         * Get all media with optional pagination, sorting, and search.
         * Returns lightweight MediaListResponse with sortConfig for dynamic sort UI.
         */
        @GetMapping
        @Operation(summary = "List all media", description = "Retrieves a paginated list of all media files with minimal data for grid display. Supports search across originalName and mimeType.")
        public ResponseEntity<ApiResponse<PageableResponse<MediaListResponse>>> getAllMedia(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @Parameter(description = "Sort field and direction (e.g., createdAt,desc)") @RequestParam(required = false) String sort,
                        @Parameter(description = "Search query (min 2 chars, searches originalName and mimeType)") @RequestParam(required = false) String search,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        // Parse and validate sort parameter
                        String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
                                        SortableFieldsConfig.MEDIA_DEFAULT_SORT);
                        Sort sortObj = SortParseUtil.parse(effectiveSort, SortableFieldsConfig.MEDIA_ALLOWED_FIELDS,
                                        SortableFieldsConfig.MEDIA_DEFAULT_SORT);

                        PageRequest pageRequest = PageRequest.of(page, size, sortObj);

                        // Use search method (handles null/short queries internally)
                        Page<Media> mediaPage = mediaService.search(pageRequest, search);

                        // Map to response DTOs
                        List<MediaListResponse> content = mediaPage.getContent().stream()
                                        .map(MediaListResponse::from)
                                        .toList();

                        // Build sortConfig
                        SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.MEDIA_SORT_OPTIONS);

                        PageableResponse<MediaListResponse> response = PageableResponse.fromMapped(mediaPage, content,
                                        sortConfig);
                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (IllegalArgumentException ex) {
                        log.warn("Invalid sort parameter: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.sort.invalid",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error getting all media: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Update media metadata (public flag, tags).
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}")
        @Operation(summary = "Update media metadata", description = "Updates media properties like public status and tags.")
        public ResponseEntity<ApiResponse<MediaResponse>> updateMedia(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid MediaUpdateRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Media media = mediaService.updateMetadata(id, request.isPublic(), request.tags());
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

        @PreAuthorize("hasRole('TENANT_ADMIN')")
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

                        // Cache for 30 days - media files are immutable (UUID-named)
                        return ResponseEntity.ok()
                                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
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
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        Optional<Media> mediaOpt = mediaService.findById(id);
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

        // ========== Format Generation Operations ==========

        /**
         * Generate a single format variant for a media file.
         * Supports both preset formats and custom dimensions.
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/generate-format")
        @Operation(summary = "Generate format variant", description = "Generates a single format variant. Use formatCode for presets or customWidth/customHeight for custom sizes.")
        public ResponseEntity<ApiResponse<MediaVariantResponse>> generateFormat(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid GenerateFormatRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        Media variant = processingService.generateSingleFormat(id, request);
                        MediaVariantResponse response = toVariantResponse(variant, request);
                        String message = messageSource.getMessage("media.format.generate.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, response));
                } catch (IllegalArgumentException ex) {
                        log.warn("Format generation validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.format.generate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error generating format for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.format.generate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Generate multiple format variants for a media file in batch.
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/generate-formats")
        @Operation(summary = "Generate multiple format variants", description = "Generates multiple format variants in a single request. Max 10 formats per request.")
        public ResponseEntity<ApiResponse<List<MediaVariantResponse>>> generateFormats(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid GenerateFormatsRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        List<MediaVariantResponse> responses = new java.util.ArrayList<>();
                        for (GenerateFormatRequest formatReq : request.formats()) {
                                Media variant = processingService.generateSingleFormat(id, formatReq);
                                responses.add(toVariantResponse(variant, formatReq));
                        }
                        String message = messageSource.getMessage("media.formats.generate.success",
                                        new Object[] { responses.size() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, responses));
                } catch (IllegalArgumentException ex) {
                        log.warn("Batch format generation validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.format.generate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error generating formats for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.format.generate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Delete a generated variant.
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @DeleteMapping("/{mediaId}/variants/{variantId}")
        @Operation(summary = "Delete variant", description = "Deletes a generated variant. The original media cannot be deleted via this endpoint.")
        public ResponseEntity<ApiResponse<Void>> deleteVariant(
                        @PathVariable @Valid @NotNull @Min(1) Long mediaId,
                        @PathVariable @Valid @NotNull @Min(1) Long variantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        processingService.deleteVariant(mediaId, variantId);
                        String message = messageSource.getMessage("media.variant.delete.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (IllegalArgumentException ex) {
                        log.warn("Variant delete validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.variant.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error deleting variant {} from media {}: {}", variantId, mediaId, ex.getMessage());
                        String message = messageSource.getMessage("media.variant.delete.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        /**
         * Update focal point for smart cropping.
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}/focal-point")
        @Operation(summary = "Update focal point", description = "Sets the focal point for smart cropping. Values are normalized (0.0 to 1.0).")
        public ResponseEntity<ApiResponse<Void>> updateFocalPoint(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestBody @Valid FocalPointRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        mediaService.updateFocalPoint(id, request.x(), request.y());
                        String message = messageSource.getMessage("media.focalpoint.update.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (IllegalArgumentException ex) {
                        log.warn("Focal point update validation error: {}", ex.getMessage());
                        String message = messageSource.getMessage("media.focalpoint.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error updating focal point for media {}: {}", id, ex.getMessage());
                        String message = messageSource.getMessage("media.focalpoint.update.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Validation ==========

        private MediaVariantResponse toVariantResponse(Media variant, GenerateFormatRequest request) {
                String formatCode = request.isPreset() ? request.formatCode()
                                : "CUSTOM_" + variant.getWidth() + "x" + variant.getHeight();
                String formatName = request.isPreset() ? request.formatCode()
                                : "Custom " + variant.getWidth() + "x" + variant.getHeight();
                return new MediaVariantResponse(
                                variant.getId(),
                                variant.getUid(),
                                formatCode,
                                formatName,
                                variant.getWidth(),
                                variant.getHeight(),
                                variant.getFileSize(),
                                variant.getFileSizeFormatted(),
                                variant.getMimeType(),
                                variant.getPublicUrl());
        }

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
