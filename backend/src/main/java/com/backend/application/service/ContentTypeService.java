package com.backend.application.service;

import com.backend.domain.entity.ContentType;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface ContentTypeService {
    
    // Basic CRUD operations
    ContentType createContentType(ContentType contentType);
    Optional<ContentType> getContentTypeById(Long id);
    ContentType updateContentType(ContentType contentType);
    void deleteContentType(Long id);
    List<ContentType> getAllContentTypes();
    
    // Tenant-specific operations
    List<ContentType> getContentTypesByTenantId(Long tenantId);
    long countContentTypesByTenantId(Long tenantId);
    
    // Name and slug operations
    Optional<ContentType> getContentTypeByName(String name, Long tenantId);
    boolean isNameAvailable(String name, Long tenantId);
    
    // Multi-language operations
    List<ContentType> getMultiLanguageContentTypes(Long tenantId);
    List<ContentType> getSingleLanguageContentTypes(Long tenantId);
    ContentType toggleMultiLanguageSupport(Long id, boolean supportsMultiLanguage);
    
    // Field schema operations
    ContentType updateFieldSchema(Long id, String fieldsJson);
    String getFieldSchema(Long id);
    boolean isValidFieldSchema(String fieldsJson);
    List<String> validateFieldSchema(String fieldsJson);
    
    // Content operations
    long getContentCountByType(Long contentTypeId);
    long getPublishedContentCountByType(Long contentTypeId);
    boolean hasContent(Long contentTypeId);
    boolean canDelete(Long contentTypeId);
    
    // Default content types operations
    List<ContentType> createDefaultContentTypes(Long tenantId);
    ContentType createArticleType(Long tenantId);
    ContentType createPageType(Long tenantId);
    ContentType createProductType(Long tenantId);
    
    // Template operations
    List<String> getAvailableTemplates(Long contentTypeId);
    ContentType updateTemplates(Long id, List<String> templates);
    
    // Usage statistics
    List<ContentType> getMostUsedContentTypes(Long tenantId, int limit);
    List<ContentType> getUnusedContentTypes(Long tenantId);
    
    // Validation
    List<String> validateContentType(ContentType contentType);
    boolean isValidForLanguage(Long contentTypeId, Language language);
    
    // Bulk operations
    void bulkDelete(List<Long> contentTypeIds);
    void deleteContentTypesByTenantId(Long tenantId);
    
    // Export and import operations
    List<ContentType> exportContentTypes(Long tenantId);
    ContentType duplicateContentType(Long contentTypeId, String newName);
}