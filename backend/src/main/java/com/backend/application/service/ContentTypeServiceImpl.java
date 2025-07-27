package com.backend.application.service;

import com.backend.domain.entity.ContentType;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ContentTypeRepository;
import com.backend.domain.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContentTypeServiceImpl implements ContentTypeService {
    
    private final ContentTypeRepository contentTypeRepository;
    private final ContentRepository contentRepository;
    
    @Override
    public ContentType createContentType(ContentType contentType) {
        log.debug("Creating new content type with name: {}", contentType.getName());
        
        // Validate unique name for tenant
        if (contentTypeRepository.existsByNameAndTenantId(contentType.getName(), contentType.getTenantId())) {
            throw new IllegalArgumentException("Content type with name '" + contentType.getName() + "' already exists for this tenant");
        }
        
        // Set defaults
        if (contentType.getSupportsMultiLanguage() == null) {
            contentType.setSupportsMultiLanguage(true);
        }
        
        ContentType savedContentType = contentTypeRepository.save(contentType);
        log.info("Content type created successfully with ID: {}", savedContentType.getId());
        
        return savedContentType;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContentType> getContentTypeById(Long id) {
        return contentTypeRepository.findById(id);
    }
    
    @Override
    public ContentType updateContentType(ContentType contentType) {
        log.debug("Updating content type with ID: {}", contentType.getId());
        
        ContentType existingContentType = contentTypeRepository.findById(contentType.getId())
            .orElseThrow(() -> new IllegalArgumentException("Content type not found with ID: " + contentType.getId()));
        
        // Check name uniqueness if name is being changed
        if (!existingContentType.getName().equals(contentType.getName()) &&
            contentTypeRepository.existsByNameAndTenantId(contentType.getName(), contentType.getTenantId())) {
            throw new IllegalArgumentException("Content type with name '" + contentType.getName() + "' already exists for this tenant");
        }
        
        ContentType updatedContentType = contentTypeRepository.save(contentType);
        log.info("Content type updated successfully with ID: {}", updatedContentType.getId());
        
        return updatedContentType;
    }
    
    @Override
    public void deleteContentType(Long id) {
        log.debug("Deleting content type with ID: {}", id);
        
        if (!contentTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Content type not found with ID: " + id);
        }
        
        if (!canDelete(id)) {
            throw new IllegalStateException("Content type cannot be deleted because it has associated content");
        }
        
        contentTypeRepository.deleteById(id);
        log.info("Content type deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypes() {
        return contentTypeRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getContentTypesByTenantId(Long tenantId) {
        return contentTypeRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countContentTypesByTenantId(Long tenantId) {
        return contentTypeRepository.countByTenantId(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContentType> getContentTypeByName(String name, Long tenantId) {
        return contentTypeRepository.findByNameAndTenantId(name, tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name, Long tenantId) {
        return !contentTypeRepository.existsByNameAndTenantId(name, tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getMultiLanguageContentTypes(Long tenantId) {
        return contentTypeRepository.findByTenantIdAndSupportsMultiLanguageTrue(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getSingleLanguageContentTypes(Long tenantId) {
        return contentTypeRepository.findByTenantIdAndSupportsMultiLanguageFalse(tenantId);
    }
    
    @Override
    public ContentType toggleMultiLanguageSupport(Long id, boolean supportsMultiLanguage) {
        ContentType contentType = contentTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Content type not found"));
        
        contentType.setSupportsMultiLanguage(supportsMultiLanguage);
        
        ContentType updatedContentType = contentTypeRepository.save(contentType);
        log.info("Multi-language support {} for content type: {}", 
                 supportsMultiLanguage ? "enabled" : "disabled", id);
        
        return updatedContentType;
    }
    
    @Override
    public ContentType updateFieldSchema(Long id, String fieldsJson) {
        ContentType contentType = contentTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Content type not found"));
        
        if (!isValidFieldSchema(fieldsJson)) {
            throw new IllegalArgumentException("Invalid field schema format");
        }
        
        contentType.setFields(fieldsJson);
        
        ContentType updatedContentType = contentTypeRepository.save(contentType);
        log.info("Field schema updated for content type: {}", id);
        
        return updatedContentType;
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getFieldSchema(Long id) {
        ContentType contentType = contentTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Content type not found"));
        
        return contentType.getFields();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isValidFieldSchema(String fieldsJson) {
        if (fieldsJson == null || fieldsJson.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Basic JSON validation - in a real implementation, you'd use a JSON parser
            return fieldsJson.trim().startsWith("{") && fieldsJson.trim().endsWith("}");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> validateFieldSchema(String fieldsJson) {
        List<String> errors = new ArrayList<>();
        
        if (fieldsJson == null || fieldsJson.trim().isEmpty()) {
            errors.add("Field schema cannot be empty");
            return errors;
        }
        
        if (!isValidFieldSchema(fieldsJson)) {
            errors.add("Invalid JSON format");
        }
        
        return errors;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getContentCountByType(Long contentTypeId) {
        return contentRepository.countByContentTypeId(contentTypeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getPublishedContentCountByType(Long contentTypeId) {
        return contentRepository.countByContentTypeIdAndStatus(contentTypeId, 
            com.backend.domain.enums.ContentStatus.PUBLISHED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasContent(Long contentTypeId) {
        return contentRepository.existsByContentTypeId(contentTypeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(Long contentTypeId) {
        return !hasContent(contentTypeId);
    }
    
    @Override
    public List<ContentType> createDefaultContentTypes(Long tenantId) {
        List<ContentType> defaultTypes = new ArrayList<>();
        
        // Only create if they don't already exist
        if (!contentTypeRepository.existsByNameAndTenantId("article", tenantId)) {
            defaultTypes.add(createArticleType(tenantId));
        }
        
        if (!contentTypeRepository.existsByNameAndTenantId("page", tenantId)) {
            defaultTypes.add(createPageType(tenantId));
        }
        
        if (!contentTypeRepository.existsByNameAndTenantId("product", tenantId)) {
            defaultTypes.add(createProductType(tenantId));
        }
        
        log.info("Created {} default content types for tenant: {}", defaultTypes.size(), tenantId);
        return defaultTypes;
    }
    
    @Override
    public ContentType createArticleType(Long tenantId) {
        ContentType articleType = new ContentType();
        articleType.setName("article");
        articleType.setDisplayName("Article");
        articleType.setFields("{\"title\":{\"type\":\"text\",\"required\":true},\"content\":{\"type\":\"richtext\",\"required\":true},\"excerpt\":{\"type\":\"textarea\",\"required\":false}}");
        articleType.setTenantId(tenantId);
        articleType.setSupportsMultiLanguage(true);
        
        return contentTypeRepository.save(articleType);
    }
    
    @Override
    public ContentType createPageType(Long tenantId) {
        ContentType pageType = new ContentType();
        pageType.setName("page");
        pageType.setDisplayName("Page");
        pageType.setFields("{\"title\":{\"type\":\"text\",\"required\":true},\"content\":{\"type\":\"richtext\",\"required\":true},\"template\":{\"type\":\"select\",\"options\":[\"default\",\"full-width\",\"sidebar\"],\"required\":false}}");
        pageType.setTenantId(tenantId);
        pageType.setSupportsMultiLanguage(true);
        
        return contentTypeRepository.save(pageType);
    }
    
    @Override
    public ContentType createProductType(Long tenantId) {
        ContentType productType = new ContentType();
        productType.setName("product");
        productType.setDisplayName("Product");
        productType.setFields("{\"name\":{\"type\":\"text\",\"required\":true},\"description\":{\"type\":\"richtext\",\"required\":true},\"price\":{\"type\":\"number\",\"required\":true},\"images\":{\"type\":\"media\",\"multiple\":true}}");
        productType.setTenantId(tenantId);
        productType.setSupportsMultiLanguage(true);
        
        return contentTypeRepository.save(productType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> validateContentType(ContentType contentType) {
        List<String> errors = new ArrayList<>();
        
        if (contentType.getName() == null || contentType.getName().trim().isEmpty()) {
            errors.add("Name is required");
        }
        
        if (contentType.getDisplayName() == null || contentType.getDisplayName().trim().isEmpty()) {
            errors.add("Display name is required");
        }
        
        if (contentType.getTenantId() == null) {
            errors.add("Tenant ID is required");
        }
        
        if (contentType.getFields() != null && !isValidFieldSchema(contentType.getFields())) {
            errors.add("Invalid field schema format");
        }
        
        return errors;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isValidForLanguage(Long contentTypeId, Language language) {
        ContentType contentType = contentTypeRepository.findById(contentTypeId)
            .orElseThrow(() -> new IllegalArgumentException("Content type not found"));
        
        return contentType.getSupportsMultiLanguage() || language != null;
    }
    
    // Placeholder implementations for interface compliance
    @Override public List<String> getAvailableTemplates(Long contentTypeId) { return List.of("default", "custom"); }
    @Override public ContentType updateTemplates(Long id, List<String> templates) { return null; }
    @Override public List<ContentType> getMostUsedContentTypes(Long tenantId, int limit) { return List.of(); }
    @Override public List<ContentType> getUnusedContentTypes(Long tenantId) { return List.of(); }
    @Override public void bulkDelete(List<Long> contentTypeIds) { }
    @Override public void deleteContentTypesByTenantId(Long tenantId) { }
    @Override public List<ContentType> exportContentTypes(Long tenantId) { return List.of(); }
    @Override public ContentType duplicateContentType(Long contentTypeId, String newName) { return null; }
}