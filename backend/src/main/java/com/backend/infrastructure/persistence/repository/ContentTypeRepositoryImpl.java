package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ContentType;
import com.backend.domain.repository.ContentTypeRepository;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContentTypeRepositoryImpl implements ContentTypeRepository {
    
    private final ContentTypeJpaRepository contentTypeJpaRepository;
    
    @Override
    public ContentType save(ContentType contentType) {
        return contentTypeJpaRepository.save(contentType);
    }
    
    @Override
    public Optional<ContentType> findById(Long id) {
        return contentTypeJpaRepository.findById(id);
    }
    
    @Override
    public List<ContentType> findAll() {
        return contentTypeJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        contentTypeJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return contentTypeJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return contentTypeJpaRepository.count();
    }
    
    // Tenant-specific queries
    @Override
    public List<ContentType> findByTenantId(Long tenantId) {
        return contentTypeJpaRepository.findByTenantId(tenantId);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndIsActiveTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndIsActive(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdOrderBySortOrderAsc(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
    }
    
    @Override
    public long countByTenantId(Long tenantId) {
        return contentTypeJpaRepository.countByTenantId(tenantId);
    }
    
    // Name and identification queries
    @Override
    public Optional<ContentType> findByName(String name) {
        return contentTypeJpaRepository.findByName(name);
    }
    
    @Override
    public Optional<ContentType> findByTenantIdAndName(Long tenantId, String name) {
        return contentTypeJpaRepository.findByTenantIdAndName(tenantId, name);
    }
    
    @Override
    public boolean existsByTenantIdAndName(Long tenantId, String name) {
        return contentTypeJpaRepository.existsByTenantIdAndName(tenantId, name);
    }
    
    // System type queries
    @Override
    public List<ContentType> findByIsSystemTypeTrue() {
        return contentTypeJpaRepository.findByIsSystemType(true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndIsSystemTypeTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndIsSystemType(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndIsSystemTypeFalse(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndIsSystemType(tenantId, false);
    }
    
    // Feature-based queries
    @Override
    public List<ContentType> findBySupportsMultiLanguageTrue() {
        return contentTypeJpaRepository.findBySupportsMultiLanguage(true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndSupportsMultiLanguageTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndSupportsMultiLanguage(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndSupportsSeoTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndSupportsSeo(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndSupportsSchedulingTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndSupportsScheduling(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndSupportsCommentsTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndSupportsComments(tenantId, true);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndRequiresApprovalTrue(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndRequiresApproval(tenantId, true);
    }
    
    // Status queries
    @Override
    public List<ContentType> findByIsActiveTrue() {
        return contentTypeJpaRepository.findByIsActive(true);
    }
    
    @Override
    public List<ContentType> findByIsActiveFalse() {
        return contentTypeJpaRepository.findByIsActive(false);
    }
    
    // Limit queries
    @Override
    public List<ContentType> findByMaxItemsIsNotNull() {
        return contentTypeJpaRepository.findByMaxItemsIsNotNull();
    }
    
    @Override
    public List<ContentType> findByTenantIdAndMaxItemsIsNotNull(Long tenantId) {
        return contentTypeJpaRepository.findByTenantIdAndMaxItemsIsNotNull(tenantId);
    }
    
    // Search queries
    @Override
    public List<ContentType> findByTenantIdAndDisplayNameContainingIgnoreCase(Long tenantId, String displayName) {
        return contentTypeJpaRepository.findByTenantIdAndDisplayNameContainingIgnoreCase(tenantId, displayName);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name) {
        return contentTypeJpaRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, name);
    }
    
    // Date queries
    @Override
    public List<ContentType> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return contentTypeJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return contentTypeJpaRepository.findByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
    }
    
    // Creator queries
    @Override
    public List<ContentType> findByCreatedBy(Long userId) {
        return contentTypeJpaRepository.findByCreatedBy(userId);
    }
    
    @Override
    public List<ContentType> findByTenantIdAndCreatedBy(Long tenantId, Long userId) {
        return contentTypeJpaRepository.findByTenantIdAndCreatedBy(tenantId, userId);
    }
    
    // Bulk operations
    @Override
    public List<ContentType> findByIdIn(List<Long> ids) {
        return contentTypeJpaRepository.findByIdIn(ids);
    }
    
    @Override
    public void deleteByTenantId(Long tenantId) {
        contentTypeJpaRepository.deleteByTenantId(tenantId);
    }
    
    // Sorting and organization
    @Override
    public List<ContentType> findByTenantIdAndIsActiveTrueOrderBySortOrderAscDisplayNameAsc(Long tenantId) {
        // This method is not available in JPA repository, so use a simpler version
        return contentTypeJpaRepository.findByTenantIdAndIsActiveOrderBySortOrderAsc(tenantId, true);
    }
    
    // Statistics
    @Override
    public long countByTenantIdAndIsActiveTrue(Long tenantId) {
        return contentTypeJpaRepository.countByTenantIdAndIsActive(tenantId, true);
    }
    
    @Override
    public long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return contentTypeJpaRepository.countByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
    }
}