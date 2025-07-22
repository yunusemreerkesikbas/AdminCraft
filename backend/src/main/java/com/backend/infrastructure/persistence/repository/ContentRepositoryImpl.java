package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Content;
import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {
    
    private final ContentJpaRepository contentJpaRepository;
    
    @Override
    public Content save(Content content) {
        return contentJpaRepository.save(content);
    }
    
    @Override
    public List<Content> saveAll(Iterable<Content> contents) {
        return contentJpaRepository.saveAll(contents);
    }
    
    @Override
    public Optional<Content> findById(Long id) {
        return contentJpaRepository.findById(id);
    }
    
    @Override
    public List<Content> findAll() {
        return contentJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        contentJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return contentJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return contentJpaRepository.count();
    }
    
    @Override
    public List<Content> findByTenantId(Long tenantId) {
        return contentJpaRepository.findByTenantId(tenantId);
    }
    
    @Override
    public List<Content> findByTenantIdOrderByCreatedAtDesc(Long tenantId) {
        return contentJpaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Override
    public long countByTenantId(Long tenantId) {
        return contentJpaRepository.countByTenantId(tenantId);
    }
    
    @Override
    public List<Content> findByContentTypeId(Long contentTypeId) {
        return contentJpaRepository.findByContentTypeId(contentTypeId);
    }
    
    @Override
    public List<Content> findByTenantIdAndContentTypeId(Long tenantId, Long contentTypeId) {
        return contentJpaRepository.findByTenantIdAndContentTypeId(tenantId, contentTypeId);
    }
    
    @Override
    public long countByTenantIdAndContentTypeId(Long tenantId, Long contentTypeId) {
        return contentJpaRepository.countByTenantIdAndContentTypeId(tenantId, contentTypeId);
    }
    
    @Override
    public List<Content> findByStatus(ContentStatus status) {
        return contentJpaRepository.findByStatus(status);
    }
    
    @Override
    public List<Content> findByTenantIdAndStatus(Long tenantId, ContentStatus status) {
        return contentJpaRepository.findByTenantIdAndStatus(tenantId, status);
    }
    
    @Override
    public List<Content> findByStatusIn(List<ContentStatus> statuses) {
        return contentJpaRepository.findByStatusIn(statuses);
    }
    
    @Override
    public List<Content> findByTenantIdAndStatusIn(Long tenantId, List<ContentStatus> statuses) {
        return contentJpaRepository.findByTenantIdAndStatusIn(tenantId, statuses);
    }
    
    @Override
    public long countByTenantIdAndStatus(Long tenantId, ContentStatus status) {
        return contentJpaRepository.countByTenantIdAndStatus(tenantId, status);
    }
    
    @Override
    public List<Content> findByLanguage(Language language) {
        return contentJpaRepository.findByLanguage(language);
    }
    
    @Override
    public List<Content> findByTenantIdAndLanguage(Long tenantId, Language language) {
        return contentJpaRepository.findByTenantIdAndLanguage(tenantId, language);
    }
    
    @Override
    public List<Content> findByTenantIdAndLanguageAndStatus(Long tenantId, Language language, ContentStatus status) {
        return contentJpaRepository.findByTenantIdAndLanguageAndStatus(tenantId, language, status);
    }
    
    @Override
    public List<Content> findByParentContentId(Long parentContentId) {
        return contentJpaRepository.findByParentContentId(parentContentId);
    }
    
    @Override
    public List<Content> findByParentContentIdIsNull() {
        return contentJpaRepository.findByParentContentIdIsNull();
    }
    
    @Override
    public List<Content> findByTenantIdAndParentContentIdIsNull(Long tenantId) {
        return contentJpaRepository.findByTenantIdAndParentContentIdIsNull(tenantId);
    }
    
    @Override
    public Optional<Content> findByParentContentIdAndLanguage(Long parentContentId, Language language) {
        return contentJpaRepository.findByParentContentIdAndLanguage(parentContentId, language);
    }
    
    @Override
    public boolean existsByParentContentIdAndLanguage(Long parentContentId, Language language) {
        return contentJpaRepository.existsByParentContentIdAndLanguage(parentContentId, language);
    }
    
    @Override
    public Optional<Content> findBySlug(String slug) {
        return contentJpaRepository.findBySlug(slug);
    }
    
    @Override
    public Optional<Content> findByTenantIdAndSlug(Long tenantId, String slug) {
        return contentJpaRepository.findByTenantIdAndSlug(tenantId, slug);
    }
    
    @Override
    public Optional<Content> findByTenantIdAndSlugAndLanguage(Long tenantId, String slug, Language language) {
        return contentJpaRepository.findByTenantIdAndSlugAndLanguage(tenantId, slug, language);
    }
    
    @Override
    public boolean existsByTenantIdAndSlug(Long tenantId, String slug) {
        return contentJpaRepository.existsByTenantIdAndSlug(tenantId, slug);
    }
    
    // Implementing remaining methods - delegating to JPA repository
    // Due to space constraints, implementing key methods and placeholders for others
    
    @Override
    public List<Content> findByStatusAndPublishedAtBefore(ContentStatus status, LocalDateTime dateTime) {
        return contentJpaRepository.findByStatusAndPublishedAtBefore(status, dateTime);
    }
    
    @Override
    public List<Content> findByStatusAndScheduledAtBefore(ContentStatus status, LocalDateTime dateTime) {
        return contentJpaRepository.findByStatusAndScheduledAtBefore(status, dateTime);
    }
    
    @Override
    public List<Content> findByTenantIdAndStatusAndPublishedAtBefore(Long tenantId, ContentStatus status, LocalDateTime dateTime) {
        return contentJpaRepository.findByTenantIdAndStatusAndPublishedAtBefore(tenantId, status, dateTime);
    }
    
    @Override
    public List<Content> findByTenantIdAndIsFeaturedTrue(Long tenantId) {
        return contentJpaRepository.findByTenantIdAndIsFeaturedTrue(tenantId);
    }
    
    @Override
    public List<Content> findByTenantIdAndIsStickyTrue(Long tenantId) {
        return contentJpaRepository.findByTenantIdAndIsStickyTrue(tenantId);
    }
    
    @Override
    public List<Content> findByTenantIdAndIsFeaturedTrueAndStatus(Long tenantId, ContentStatus status) {
        return contentJpaRepository.findByTenantIdAndIsFeaturedTrueAndStatus(tenantId, status);
    }
    
    @Override
    public List<Content> findByTenantIdAndTitleContainingIgnoreCase(Long tenantId, String title) {
        return contentJpaRepository.findByTenantIdAndTitleContainingIgnoreCase(tenantId, title);
    }
    
    @Override
    public List<Content> findByTenantIdAndDataContainingIgnoreCase(Long tenantId, String searchTerm) {
        return contentJpaRepository.findByTenantIdAndDataContainingIgnoreCase(tenantId, searchTerm);
    }
    
    @Override
    public List<Content> findByTenantIdAndLanguageAndTitleContainingIgnoreCase(Long tenantId, Language language, String title) {
        return contentJpaRepository.findByTenantIdAndLanguageAndTitleContainingIgnoreCase(tenantId, language, title);
    }
    
    // Placeholder implementations for remaining methods
    @Override public List<Content> findByTenantIdAndMetaTitleIsNotNull(Long tenantId) { return contentJpaRepository.findByTenantIdAndMetaTitleIsNotNull(tenantId); }
    @Override public List<Content> findByTenantIdAndMetaDescriptionIsNotNull(Long tenantId) { return contentJpaRepository.findByTenantIdAndMetaDescriptionIsNotNull(tenantId); }
    @Override public List<Content> findByTenantIdAndNoIndexTrue(Long tenantId) { return contentJpaRepository.findByTenantIdAndNoIndexTrue(tenantId); }
    @Override public List<Content> findByCreatedBy(Long userId) { return contentJpaRepository.findByCreatedBy(userId); }
    @Override public List<Content> findByTenantIdAndCreatedBy(Long tenantId, Long userId) { return contentJpaRepository.findByTenantIdAndCreatedBy(tenantId, userId); }
    @Override public List<Content> findByPublishedBy(Long userId) { return contentJpaRepository.findByPublishedBy(userId); }
    @Override public List<Content> findByTenantIdAndUpdatedBy(Long tenantId, Long userId) { return contentJpaRepository.findByTenantIdAndUpdatedBy(tenantId, userId); }
    @Override public List<Content> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.findByCreatedAtBetween(startDate, endDate); }
    @Override public List<Content> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.findByPublishedAtBetween(startDate, endDate); }
    @Override public List<Content> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.findByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate); }
    @Override public List<Content> findByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.findByTenantIdAndPublishedAtBetween(tenantId, startDate, endDate); }
    @Override public List<Content> findByExpiresAtBefore(LocalDateTime dateTime) { return contentJpaRepository.findByExpiresAtBefore(dateTime); }
    @Override public List<Content> findByTenantIdAndExpiresAtBefore(Long tenantId, LocalDateTime dateTime) { return contentJpaRepository.findByTenantIdAndExpiresAtBefore(tenantId, dateTime); }
    @Override public List<Content> findByTenantIdAndExpiresAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.findByTenantIdAndExpiresAtBetween(tenantId, startDate, endDate); }
    @Override public List<Content> findByTenantIdOrderByViewCountDesc(Long tenantId) { return contentJpaRepository.findByTenantIdOrderByViewCountDesc(tenantId); }
    @Override public List<Content> findByTenantIdAndStatusOrderByViewCountDesc(Long tenantId, ContentStatus status) { return contentJpaRepository.findByTenantIdAndStatusOrderByViewCountDesc(tenantId, status); }
    @Override public List<Content> findByIdIn(List<Long> ids) { return contentJpaRepository.findByIdIn(ids); }
    @Override public void deleteByTenantId(Long tenantId) { contentJpaRepository.deleteByTenantId(tenantId); }
    @Override public void deleteByContentTypeId(Long contentTypeId) { contentJpaRepository.deleteByContentTypeId(contentTypeId); }
    @Override public List<Content> findByTenantIdOrderBySortOrderAscCreatedAtDesc(Long tenantId) { return contentJpaRepository.findByTenantIdOrderBySortOrderAscCreatedAtDesc(tenantId); }
    @Override public List<Content> findByTenantIdAndStatusOrderBySortOrderAscCreatedAtDesc(Long tenantId, ContentStatus status) { return contentJpaRepository.findByTenantIdAndStatusOrderBySortOrderAscCreatedAtDesc(tenantId, status); }
    @Override public List<Content> findByTenantIdAndLanguageOrderByCreatedAtDesc(Long tenantId, Language language) { return contentJpaRepository.findByTenantIdAndLanguageOrderByCreatedAtDesc(tenantId, language); }
    @Override public long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.countByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate); }
    @Override public long countByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) { return contentJpaRepository.countByTenantIdAndPublishedAtBetween(tenantId, startDate, endDate); }
    @Override public long countByTenantIdAndLanguageAndStatus(Long tenantId, Language language, ContentStatus status) { return contentJpaRepository.countByTenantIdAndLanguageAndStatus(tenantId, language, status); }
    @Override public List<Content> findByTenantIdAndTemplate(Long tenantId, String template) { return contentJpaRepository.findByTenantIdAndTemplate(tenantId, template); }
    @Override public List<Content> findByTenantIdAndLayout(Long tenantId, String layout) { return contentJpaRepository.findByTenantIdAndLayout(tenantId, layout); }
    @Override public List<Content> findByTenantIdAndIsPasswordProtectedTrue(Long tenantId) { return contentJpaRepository.findByTenantIdAndIsPasswordProtectedTrue(tenantId); }
    @Override public List<Content> findByTenantIdAndRequiresLoginTrue(Long tenantId) { return contentJpaRepository.findByTenantIdAndRequiresLoginTrue(tenantId); }
}