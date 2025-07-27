package com.backend.presentation.controller;

import com.backend.application.service.ContentTypeService;
import com.backend.domain.entity.ContentType;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.mapper.ContentTypeMapper;
import com.backend.presentation.dto.response.ContentTypeResponse;
import com.backend.shared.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/content-types")
public class ContentTypeController {

    @Autowired
    private ContentTypeService contentTypeService;

    @Autowired
    private ContentTypeMapper contentTypeMapper;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentTypeResponse>>> getAllContentTypes(
            @RequestParam(required = false) Boolean multiLanguage,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<ContentType> contentTypes;
            if (multiLanguage != null && multiLanguage) {
                contentTypes = contentTypeService.getMultiLanguageContentTypes(tenantId);
            } else if (multiLanguage != null && !multiLanguage) {
                contentTypes = contentTypeService.getSingleLanguageContentTypes(tenantId);
            } else {
                contentTypes = contentTypeService.getContentTypesByTenantId(tenantId);
            }
            
            List<ContentTypeResponse> responses = contentTypes.stream()
                .map(contentType -> {
                    long contentCount = contentTypeService.getContentCountByType(contentType.getId());
                    long publishedContentCount = contentTypeService.getPublishedContentCountByType(contentType.getId());
                    return contentTypeMapper.toResponse(contentType, contentCount, publishedContentCount);
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentTypeResponse>> getContentTypeById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<ContentType> contentTypeOpt = contentTypeService.getContentTypeById(id);
            if (contentTypeOpt.isEmpty()) {
                String message = messageSource.getMessage("content.type.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            ContentType contentType = contentTypeOpt.get();
            
            // Get additional statistics
            long contentCount = contentTypeService.getContentCountByType(contentType.getId());
            long publishedContentCount = contentTypeService.getPublishedContentCountByType(contentType.getId());
            
            ContentTypeResponse response = contentTypeMapper.toResponse(contentType, contentCount, publishedContentCount);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ContentTypeResponse>> getContentTypeByName(
            @PathVariable String name,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            Optional<ContentType> contentTypeOpt = contentTypeService.getContentTypeByName(name, tenantId);
            if (contentTypeOpt.isEmpty()) {
                String message = messageSource.getMessage("content.type.name.not.found", new Object[]{name}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            ContentType contentType = contentTypeOpt.get();
            
            // Get additional statistics
            long contentCount = contentTypeService.getContentCountByType(contentType.getId());
            long publishedContentCount = contentTypeService.getPublishedContentCountByType(contentType.getId());
            
            ContentTypeResponse response = contentTypeMapper.toResponse(contentType, contentCount, publishedContentCount);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.name.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/name/{name}")
    public ResponseEntity<ApiResponse<Boolean>> checkNameAvailability(
            @PathVariable String name,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            boolean available = contentTypeService.isNameAvailable(name, tenantId);
            String messageKey = available ? "content.type.name.available" : "content.type.name.taken";
            String message = messageSource.getMessage(messageKey, new Object[]{name}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.name.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/default")
    public ResponseEntity<ApiResponse<List<ContentTypeResponse>>> createDefaultContentTypes(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<ContentType> defaultTypes = contentTypeService.createDefaultContentTypes(tenantId);
            
            List<ContentTypeResponse> responses = defaultTypes.stream()
                .map(contentType -> {
                    long contentCount = contentTypeService.getContentCountByType(contentType.getId());
                    long publishedContentCount = contentTypeService.getPublishedContentCountByType(contentType.getId());
                    return contentTypeMapper.toResponse(contentType, contentCount, publishedContentCount);
                })
                .toList();
            
            String message = messageSource.getMessage("content.type.defaults.created.success", new Object[]{defaultTypes.size()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.defaults.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/unused")
    public ResponseEntity<ApiResponse<List<ContentTypeResponse>>> getUnusedContentTypes(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<ContentType> unusedTypes = contentTypeService.getUnusedContentTypes(tenantId);
            
            List<ContentTypeResponse> responses = unusedTypes.stream()
                .map(contentType -> contentTypeMapper.toResponse(contentType, 0L, 0L))
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.unused.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/most-used")
    public ResponseEntity<ApiResponse<List<ContentTypeResponse>>> getMostUsedContentTypes(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            List<ContentType> mostUsedTypes = contentTypeService.getMostUsedContentTypes(tenantId, limit);
            
            List<ContentTypeResponse> responses = mostUsedTypes.stream()
                .map(contentType -> {
                    long contentCount = contentTypeService.getContentCountByType(contentType.getId());
                    long publishedContentCount = contentTypeService.getPublishedContentCountByType(contentType.getId());
                    return contentTypeMapper.toResponse(contentType, contentCount, publishedContentCount);
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.most.used.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/toggle-multilanguage")
    public ResponseEntity<ApiResponse<ContentTypeResponse>> toggleMultiLanguageSupport(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            ContentType updatedContentType = contentTypeService.toggleMultiLanguageSupport(id, enabled);
            
            // Get additional statistics
            long contentCount = contentTypeService.getContentCountByType(updatedContentType.getId());
            long publishedContentCount = contentTypeService.getPublishedContentCountByType(updatedContentType.getId());
            
            ContentTypeResponse response = contentTypeMapper.toResponse(updatedContentType, contentCount, publishedContentCount);
            
            String messageKey = enabled ? "content.type.multilanguage.enabled" : "content.type.multilanguage.disabled";
            String message = messageSource.getMessage(messageKey, null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.multilanguage.toggle.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}/schema")
    public ResponseEntity<ApiResponse<String>> getFieldSchema(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String schema = contentTypeService.getFieldSchema(id);
            return ResponseEntity.ok(ApiResponse.success(schema));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.schema.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}/schema")
    public ResponseEntity<ApiResponse<ContentTypeResponse>> updateFieldSchema(
            @PathVariable Long id,
            @RequestBody String fieldsJson,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            ContentType updatedContentType = contentTypeService.updateFieldSchema(id, fieldsJson);
            
            // Get additional statistics
            long contentCount = contentTypeService.getContentCountByType(updatedContentType.getId());
            long publishedContentCount = contentTypeService.getPublishedContentCountByType(updatedContentType.getId());
            
            ContentTypeResponse response = contentTypeMapper.toResponse(updatedContentType, contentCount, publishedContentCount);
            
            String message = messageSource.getMessage("content.type.schema.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.schema.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/validate-schema")
    public ResponseEntity<ApiResponse<List<String>>> validateFieldSchema(
            @PathVariable Long id,
            @RequestBody String fieldsJson,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<String> validationErrors = contentTypeService.validateFieldSchema(fieldsJson);
            
            if (validationErrors.isEmpty()) {
                String message = messageSource.getMessage("content.type.schema.valid", null, Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, validationErrors));
            } else {
                String message = messageSource.getMessage("content.type.schema.invalid", null, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message, validationErrors));
            }
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.schema.validate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}/can-delete")
    public ResponseEntity<ApiResponse<Boolean>> canDeleteContentType(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            boolean canDelete = contentTypeService.canDelete(id);
            String messageKey = canDelete ? "content.type.can.delete" : "content.type.cannot.delete";
            String message = messageSource.getMessage(messageKey, null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, canDelete));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.can.delete.check.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getContentTypeStats(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Long tenantId = 1L; // TODO: Get from security context
            
            var stats = new Object() {
                public final long totalContentTypes = contentTypeService.countContentTypesByTenantId(tenantId);
                public final long multiLanguageTypes = contentTypeService.getMultiLanguageContentTypes(tenantId).size();
                public final long singleLanguageTypes = contentTypeService.getSingleLanguageContentTypes(tenantId).size();
                public final long unusedTypes = contentTypeService.getUnusedContentTypes(tenantId).size();
            };
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception ex) {
            String message = messageSource.getMessage("content.type.stats.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}