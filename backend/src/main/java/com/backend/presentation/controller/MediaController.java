package com.backend.presentation.controller;

import com.backend.application.service.MediaService;
import com.backend.application.service.UserService;
import com.backend.domain.entity.MediaFile;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.mapper.MediaMapper;
import com.backend.presentation.dto.request.CreateMediaRequest;
import com.backend.presentation.dto.request.UpdateMediaRequest;
import com.backend.presentation.dto.response.MediaResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private UserService userService;

    @Autowired
    private MediaMapper mediaMapper;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMediaFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altTextTr", required = false) String altTextTr,
            @RequestParam(value = "altTextEn", required = false) String altTextEn,
            @RequestParam(value = "descriptionTr", required = false) String descriptionTr,
            @RequestParam(value = "descriptionEn", required = false) String descriptionEn,
            @RequestParam(value = "titleTr", required = false) String titleTr,
            @RequestParam(value = "titleEn", required = false) String titleEn,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            Long uploadedBy = 1L; // TODO: Get from security context
            
            // Upload file
            MediaFile uploadedFile = mediaService.uploadFile(file, tenantId, uploadedBy);
            
            // Update metadata if provided
            CreateMediaRequest metadataRequest = new CreateMediaRequest(
                altTextTr, altTextEn, descriptionTr, descriptionEn, 
                titleTr, titleEn, folder, category, tags, isPublic
            );
            
            MediaFile updatedFile = mediaMapper.toEntity(metadataRequest, uploadedFile);
            MediaFile savedFile = mediaService.updateMediaFile(updatedFile);
            
            // Get uploader for response
            Optional<User> uploader = userService.getUserById(savedFile.getUploadedBy());
            
            MediaResponse response = mediaMapper.toResponse(savedFile, uploader.orElse(null));
            
            String message = messageSource.getMessage("media.uploaded.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.upload.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaFileById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFileOpt = mediaService.getMediaFileById(id);
            if (mediaFileOpt.isEmpty()) {
                String message = messageSource.getMessage("media.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            MediaFile mediaFile = mediaFileOpt.get();
            
            // Get uploader for response
            Optional<User> uploader = userService.getUserById(mediaFile.getUploadedBy());
            
            MediaResponse response = mediaMapper.toResponse(mediaFile, uploader.orElse(null));
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getAllMediaFiles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String category,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<MediaFile> mediaFiles;
            if ("image".equals(type)) {
                mediaFiles = mediaService.getImageFiles(tenantId);
            } else if ("video".equals(type)) {
                mediaFiles = mediaService.getVideoFiles(tenantId);
            } else if ("document".equals(type)) {
                mediaFiles = mediaService.getDocumentFiles(tenantId);
            } else if (folder != null) {
                mediaFiles = mediaService.getFilesByFolder(tenantId, folder);
            } else if (category != null) {
                mediaFiles = mediaService.getFilesByCategory(tenantId, category);
            } else {
                mediaFiles = mediaService.getMediaFilesByTenantId(tenantId);
            }
            
            List<MediaResponse> responses = mediaFiles.stream()
                .map(mediaFile -> {
                    Optional<User> uploader = userService.getUserById(mediaFile.getUploadedBy());
                    return mediaMapper.toResponse(mediaFile, uploader.orElse(null));
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> updateMediaFile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMediaRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> existingFileOpt = mediaService.getMediaFileById(id);
            if (existingFileOpt.isEmpty()) {
                String message = messageSource.getMessage("media.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            MediaFile existingFile = existingFileOpt.get();
            MediaFile updatedFile = mediaMapper.toEntity(request, existingFile);
            
            MediaFile savedFile = mediaService.updateMediaFile(updatedFile);
            
            // Get uploader for response
            Optional<User> uploader = userService.getUserById(savedFile.getUploadedBy());
            
            MediaResponse response = mediaMapper.toResponse(savedFile, uploader.orElse(null));
            
            String message = messageSource.getMessage("media.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMediaFile(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            mediaService.deleteMediaFile(id);
            String message = messageSource.getMessage("media.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> searchMediaFiles(
            @RequestParam String query,
            @RequestParam(required = false) Language language,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<MediaFile> mediaFiles;
            if (language != null) {
                mediaFiles = mediaService.searchByAltText(tenantId, query, language);
            } else {
                mediaFiles = mediaService.searchByName(tenantId, query);
            }
            
            List<MediaResponse> responses = mediaFiles.stream()
                .map(mediaFile -> {
                    Optional<User> uploader = userService.getUserById(mediaFile.getUploadedBy());
                    return mediaMapper.toResponse(mediaFile, uploader.orElse(null));
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.search.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFileContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFileOpt = mediaService.getMediaFileById(id);
            if (mediaFileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MediaFile mediaFile = mediaFileOpt.get();
            byte[] content = mediaService.getFileContent(mediaFile.getFileName());
            
            return ResponseEntity.ok()
                .header("Content-Type", mediaFile.getMimeType())
                .header("Content-Disposition", "inline; filename=\"" + mediaFile.getOriginalName() + "\"")
                .body(content);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/thumbnails/{id}")
    public ResponseEntity<byte[]> getThumbnailContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFileOpt = mediaService.getMediaFileById(id);
            if (mediaFileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MediaFile mediaFile = mediaFileOpt.get();
            byte[] content = mediaService.getThumbnailContent(mediaFile.getFileName());
            
            return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(content);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/storage")
    public ResponseEntity<ApiResponse<Object>> getStorageStats(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            var stats = new Object() {
                public final long totalFiles = mediaService.countMediaFilesByTenantId(tenantId);
                public final long totalStorageUsed = mediaService.getTotalStorageUsed(tenantId);
                public final long imageFiles = mediaService.getImageFilesCount(tenantId);
                public final long videoFiles = mediaService.getVideoFilesCount(tenantId);
                public final long documentFiles = mediaService.getDocumentFilesCount(tenantId);
            };
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception ex) {
            String message = messageSource.getMessage("media.stats.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}