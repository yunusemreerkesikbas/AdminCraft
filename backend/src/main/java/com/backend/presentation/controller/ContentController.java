package com.backend.presentation.controller;

import com.backend.application.service.ContentService;
import com.backend.application.service.ContentTypeService;
import com.backend.application.service.UserService;
import com.backend.domain.entity.Content;
import com.backend.domain.entity.ContentType;
import com.backend.domain.entity.User;
import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.mapper.ContentMapper;
import com.backend.presentation.dto.request.CreateContentRequest;
import com.backend.presentation.dto.request.UpdateContentRequest;
import com.backend.presentation.dto.response.ContentResponse;
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
@RequestMapping("/contents")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentTypeService contentTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @Valid @RequestBody CreateContentRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            // Convert DTO to Entity
            Content content = contentMapper.toEntity(request);
            
            // Set tenant ID from context (would normally come from JWT token)
            content.setTenantId(1L); // TODO: Get from security context
            content.setCreatedBy(1L); // TODO: Get from security context
            
            // Create content
            Content savedContent = contentService.createContent(content);
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(savedContent.getContentTypeId());
            Optional<User> author = userService.getUserById(savedContent.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                savedContent, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            String message = messageSource.getMessage("content.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Content> contentOpt = contentService.getContentById(id);
            if (contentOpt.isEmpty()) {
                String message = messageSource.getMessage("content.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            Content content = contentOpt.get();
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(content.getContentTypeId());
            Optional<User> author = userService.getUserById(content.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                content, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getAllContents(
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) Language language,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<Content> contents;
            if (status != null) {
                contents = contentService.getContentByTenantIdAndStatus(tenantId, status);
            } else if (language != null) {
                contents = contentService.getContentByTenantIdAndLanguage(tenantId, language);
            } else {
                contents = contentService.getContentByTenantId(tenantId);
            }
            
            List<ContentResponse> responses = contents.stream()
                .map(content -> {
                    Optional<ContentType> contentType = contentTypeService.getContentTypeById(content.getContentTypeId());
                    Optional<User> author = userService.getUserById(content.getCreatedBy());
                    return contentMapper.toResponse(
                        content, 
                        contentType.orElse(null), 
                        author.map(User::getFullName).orElse(null), 
                        null
                    );
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Content> existingContentOpt = contentService.getContentById(id);
            if (existingContentOpt.isEmpty()) {
                String message = messageSource.getMessage("content.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            Content existingContent = existingContentOpt.get();
            Content updatedContent = contentMapper.toEntity(request, existingContent);
            updatedContent.setUpdatedBy(1L); // TODO: Get from security context
            
            Content savedContent = contentService.updateContent(updatedContent);
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(savedContent.getContentTypeId());
            Optional<User> author = userService.getUserById(savedContent.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                savedContent, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            String message = messageSource.getMessage("content.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            contentService.deleteContent(id);
            String message = messageSource.getMessage("content.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<ContentResponse>> publishContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long userId = 1L; // TODO: Get from security context
            Content publishedContent = contentService.publishContent(id, userId);
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(publishedContent.getContentTypeId());
            Optional<User> author = userService.getUserById(publishedContent.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                publishedContent, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            String message = messageSource.getMessage("content.published.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.publish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ContentResponse>> archiveContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long userId = 1L; // TODO: Get from security context
            Content archivedContent = contentService.archiveContent(id, userId);
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(archivedContent.getContentTypeId());
            Optional<User> author = userService.getUserById(archivedContent.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                archivedContent, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            String message = messageSource.getMessage("content.archived.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.archive.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<ContentResponse>> unpublishContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long userId = 1L; // TODO: Get from security context
            Content unpublishedContent = contentService.unpublishContent(id, userId);
            
            // Get additional data for response
            Optional<ContentType> contentType = contentTypeService.getContentTypeById(unpublishedContent.getContentTypeId());
            Optional<User> author = userService.getUserById(unpublishedContent.getCreatedBy());
            
            ContentResponse response = contentMapper.toResponse(
                unpublishedContent, 
                contentType.orElse(null), 
                author.map(User::getFullName).orElse(null), 
                null
            );
            
            String message = messageSource.getMessage("content.unpublished.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.unpublish.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }
}