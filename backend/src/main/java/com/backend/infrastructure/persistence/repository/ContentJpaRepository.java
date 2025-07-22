package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Content;
import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentJpaRepository extends JpaRepository<Content, Long> {
    
    // Tenant-specific queries
    List<Content> findByTenantId(Long tenantId);
    List<Content> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    
    // Content type queries
    List<Content> findByContentTypeId(Long contentTypeId);
    List<Content> findByTenantIdAndContentTypeId(Long tenantId, Long contentTypeId);
    long countByTenantIdAndContentTypeId(Long tenantId, Long contentTypeId);
    
    // Status queries
    List<Content> findByStatus(ContentStatus status);
    List<Content> findByTenantIdAndStatus(Long tenantId, ContentStatus status);
    List<Content> findByStatusIn(List<ContentStatus> statuses);
    List<Content> findByTenantIdAndStatusIn(Long tenantId, List<ContentStatus> statuses);
    long countByTenantIdAndStatus(Long tenantId, ContentStatus status);
    
    // Language queries
    List<Content> findByLanguage(Language language);
    List<Content> findByTenantIdAndLanguage(Long tenantId, Language language);
    List<Content> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, ContentStatus status);
    
    // Translation queries
    List<Content> findByParentContentId(Long parentContentId);
    List<Content> findByParentContentIdIsNull();
    List<Content> findByTenantIdAndParentContentIdIsNull(Long tenantId);
    Optional<Content> findByParentContentIdAndLanguage(Long parentContentId, Language language);
    boolean existsByParentContentIdAndLanguage(Long parentContentId, Language language);
    
    // Slug and URL queries
    Optional<Content> findBySlug(String slug);
    Optional<Content> findByTenantIdAndSlug(Long tenantId, String slug);
    Optional<Content> findByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language);
    boolean existsByTenantIdAndSlug(Long tenantId, String slug);
    
    // Publishing queries
    List<Content> findByStatusAndPublishedAtBefore(ContentStatus status, LocalDateTime dateTime);
    List<Content> findByStatusAndScheduledAtBefore(ContentStatus status, LocalDateTime dateTime);
    List<Content> findByTenantIdAndStatusAndPublishedAtBefore(Long tenantId, ContentStatus status, LocalDateTime dateTime);
    
    // Featured and sticky content
    List<Content> findByTenantIdAndIsFeaturedTrue(Long tenantId);
    List<Content> findByTenantIdAndIsStickyTrue(Long tenantId);
    List<Content> findByTenantIdAndIsFeaturedTrueAndStatus(Long tenantId, ContentStatus status);
    
    // Search queries
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Content> findByTenantIdAndTitleContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                           @Param("title") String title);
    
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND " +
           "LOWER(c.data) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Content> findByTenantIdAndDataContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                          @Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.language = :language AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Content> findByTenantIdAndLanguageAndTitleContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                                      @Param("language") Language language, 
                                                                      @Param("title") String title);
    
    // SEO queries
    List<Content> findByTenantIdAndMetaTitleIsNotNull(Long tenantId);
    List<Content> findByTenantIdAndMetaDescriptionIsNotNull(Long tenantId);
    List<Content> findByTenantIdAndNoIndexTrue(Long tenantId);
    
    // Author queries
    List<Content> findByCreatedBy(Long userId);
    List<Content> findByTenantIdAndCreatedBy(Long tenantId, Long userId);
    List<Content> findByPublishedBy(Long userId);
    List<Content> findByTenantIdAndUpdatedBy(Long tenantId, Long userId);
    
    // Date range queries
    List<Content> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Content> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Content> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<Content> findByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Expiration queries
    List<Content> findByExpiresAtBefore(LocalDateTime dateTime);
    List<Content> findByTenantIdAndExpiresAtBefore(Long tenantId, LocalDateTime dateTime);
    List<Content> findByTenantIdAndExpiresAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // View count queries
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId ORDER BY c.viewCount DESC")
    List<Content> findByTenantIdOrderByViewCountDesc(@Param("tenantId") Long tenantId);
    
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.status = :status ORDER BY c.viewCount DESC")
    List<Content> findByTenantIdAndStatusOrderByViewCountDesc(@Param("tenantId") Long tenantId, 
                                                            @Param("status") ContentStatus status);
    
    // Custom sorting queries
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId ORDER BY c.sortOrder ASC, c.createdAt DESC")
    List<Content> findByTenantIdOrderBySortOrderAscCreatedAtDesc(@Param("tenantId") Long tenantId);
    
    @Query("SELECT c FROM Content c WHERE c.tenantId = :tenantId AND c.status = :status ORDER BY c.sortOrder ASC, c.createdAt DESC")
    List<Content> findByTenantIdAndStatusOrderBySortOrderAscCreatedAtDesc(@Param("tenantId") Long tenantId, 
                                                                        @Param("status") ContentStatus status);
    
    List<Content> findByTenantIdAndLanguageOrderByCreatedAtDesc(Long tenantId, Language language);
    
    // Statistics
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndLanguageAndStatus(Long tenantId, Language language, ContentStatus status);
    
    // Template and layout queries
    List<Content> findByTenantIdAndTemplate(Long tenantId, String template);
    List<Content> findByTenantIdAndLayout(Long tenantId, String layout);
    
    // Access control queries
    List<Content> findByTenantIdAndIsPasswordProtectedTrue(Long tenantId);
    List<Content> findByTenantIdAndRequiresLoginTrue(Long tenantId);
    
    // Bulk operations
    List<Content> findByIdIn(List<Long> ids);
    void deleteByTenantId(Long tenantId);
    void deleteByContentTypeId(Long contentTypeId);
}