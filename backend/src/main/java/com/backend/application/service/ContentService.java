package com.backend.application.service;

import com.backend.domain.entity.Content;
import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContentService {
    
    // Basic CRUD operations
    Content createContent(Content content);
    Optional<Content> getContentById(Long id);
    Content updateContent(Content content);
    void deleteContent(Long id);
    List<Content> getAllContent();
    
    // Tenant-specific operations
    List<Content> getContentByTenantId(Long tenantId);
    List<Content> getContentByTenantIdAndStatus(Long tenantId, ContentStatus status);
    long countContentByTenantId(Long tenantId);
    
    // Content type operations
    List<Content> getContentByContentType(Long contentTypeId);
    List<Content> getContentByTenantIdAndContentType(Long tenantId, Long contentTypeId);
    long countContentByTenantIdAndContentType(Long tenantId, Long contentTypeId);
    
    // Language-specific operations
    List<Content> getContentByLanguage(Language language);
    List<Content> getContentByTenantIdAndLanguage(Long tenantId, Language language);
    List<Content> getPublishedContentByLanguage(Long tenantId, Language language);
    
    // Translation operations
    Content createTranslation(Long parentContentId, Language targetLanguage, Long userId);
    List<Content> getTranslations(Long parentContentId);
    Optional<Content> getTranslation(Long parentContentId, Language language);
    boolean hasTranslation(Long parentContentId, Language language);
    void linkTranslation(Long parentContentId, Long translationId);
    
    // Publishing operations
    Content publishContent(Long contentId, Long userId);
    Content unpublishContent(Long contentId, Long userId);
    Content scheduleContent(Long contentId, LocalDateTime scheduledAt, Long userId);
    Content archiveContent(Long contentId, Long userId);
    List<Content> getScheduledContent(Long tenantId);
    void processScheduledContent(); // Batch job method
    
    // Status management
    Content changeStatus(Long contentId, ContentStatus newStatus, Long userId);
    List<Content> getContentByStatus(ContentStatus status);
    List<Content> getDraftContent(Long tenantId, Long userId);
    List<Content> getPublishedContent(Long tenantId);
    
    // Slug and URL operations
    Optional<Content> getContentBySlug(String slug, Long tenantId);
    Optional<Content> getContentBySlugAndLanguage(String slug, Long tenantId, Language language);
    String generateUniqueSlug(String title, Long tenantId, Language language);
    boolean isSlugAvailable(String slug, Long tenantId, Language language);
    
    // Search and filtering
    List<Content> searchContent(Long tenantId, String searchTerm);
    List<Content> searchContentByLanguage(Long tenantId, String searchTerm, Language language);
    List<Content> getContentByAuthor(Long authorId);
    List<Content> getFeaturedContent(Long tenantId);
    List<Content> getStickyContent(Long tenantId);
    
    // SEO operations
    Content updateSeoFields(Long contentId, String metaTitle, String metaDescription, String metaKeywords);
    List<Content> getContentWithoutSeo(Long tenantId);
    List<Content> getContentForSitemap(Long tenantId, Language language);
    
    // View and engagement tracking
    void incrementViewCount(Long contentId);
    void incrementLikeCount(Long contentId);
    void incrementCommentCount(Long contentId);
    List<Content> getMostViewedContent(Long tenantId, int limit);
    List<Content> getTrendingContent(Long tenantId, int days, int limit);
    
    // Date range operations
    List<Content> getContentCreatedBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<Content> getContentPublishedBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<Content> getExpiredContent(Long tenantId);
    List<Content> getContentExpiringBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Bulk operations
    void bulkPublish(List<Long> contentIds, Long userId);
    void bulkUnpublish(List<Long> contentIds, Long userId);
    void bulkArchive(List<Long> contentIds, Long userId);
    void bulkDelete(List<Long> contentIds);
    void deleteContentByTenantId(Long tenantId);
    
    // Template and layout operations
    List<Content> getContentByTemplate(Long tenantId, String template);
    List<Content> getContentByLayout(Long tenantId, String layout);
    void updateTemplate(Long contentId, String template);
    void updateLayout(Long contentId, String layout);
    
    // Access control operations
    List<Content> getPasswordProtectedContent(Long tenantId);
    List<Content> getLoginRequiredContent(Long tenantId);
    void setContentPassword(Long contentId, String password);
    void removeContentPassword(Long contentId);
    void setLoginRequired(Long contentId, boolean required);
    
    // Statistics and analytics
    long getTotalContentCount();
    long getPublishedContentCount(Long tenantId);
    long getDraftContentCount(Long tenantId);
    long getContentCountByLanguage(Long tenantId, Language language);
    List<Content> getRecentContent(Long tenantId, int limit);
    List<Content> getPopularContent(Long tenantId, int limit);
    
    // Content validation
    boolean canPublish(Long contentId);
    boolean canEdit(Long contentId, Long userId);
    boolean canDelete(Long contentId, Long userId);
    List<String> validateContent(Content content);
    
    // Export and import operations
    List<Content> exportContent(Long tenantId, ContentStatus status, Language language);
    Content duplicateContent(Long contentId, Long userId);
    void restoreFromTrash(Long contentId);
}