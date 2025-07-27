package com.backend.domain.repository;

import com.backend.domain.entity.ContentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContentTypeRepository {
    
    // Basic CRUD operations
    ContentType save(ContentType contentType);
    Optional<ContentType> findById(Long id);
    List<ContentType> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    
    // Tenant-specific queries
    List<ContentType> findByTenantId(Long tenantId);
    List<ContentType> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<ContentType> findByTenantIdOrderBySortOrderAsc(Long tenantId);
    List<ContentType> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    
    // Name and identification queries
    Optional<ContentType> findByName(String name);
    Optional<ContentType> findByTenantIdAndName(Long tenantId, String name);
    Optional<ContentType> findByNameAndTenantId(String name, Long tenantId);
    boolean existsByTenantIdAndName(Long tenantId, String name);
    boolean existsByNameAndTenantId(String name, Long tenantId);
    
    // System type queries
    List<ContentType> findByIsSystemTypeTrue();
    List<ContentType> findByTenantIdAndIsSystemTypeTrue(Long tenantId);
    List<ContentType> findByTenantIdAndIsSystemTypeFalse(Long tenantId);
    
    // Feature-based queries
    List<ContentType> findBySupportsMultiLanguageTrue();
    List<ContentType> findByTenantIdAndSupportsMultiLanguageTrue(Long tenantId);
    List<ContentType> findByTenantIdAndSupportsMultiLanguageFalse(Long tenantId);
    List<ContentType> findByTenantIdAndSupportsSeoTrue(Long tenantId);
    List<ContentType> findByTenantIdAndSupportsSchedulingTrue(Long tenantId);
    List<ContentType> findByTenantIdAndSupportsCommentsTrue(Long tenantId);
    List<ContentType> findByTenantIdAndRequiresApprovalTrue(Long tenantId);
    
    // Status queries
    List<ContentType> findByIsActiveTrue();
    List<ContentType> findByIsActiveFalse();
    
    // Limit queries
    List<ContentType> findByMaxItemsIsNotNull();
    List<ContentType> findByTenantIdAndMaxItemsIsNotNull(Long tenantId);
    
    // Search queries
    List<ContentType> findByTenantIdAndDisplayNameContainingIgnoreCase(Long tenantId, String displayName);
    List<ContentType> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name);
    
    // Date queries
    List<ContentType> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ContentType> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Creator queries
    List<ContentType> findByCreatedBy(Long userId);
    List<ContentType> findByTenantIdAndCreatedBy(Long tenantId, Long userId);
    
    // Bulk operations
    List<ContentType> findByIdIn(List<Long> ids);
    void deleteByTenantId(Long tenantId);
    
    // Sorting and organization
    List<ContentType> findByTenantIdAndIsActiveTrueOrderBySortOrderAscDisplayNameAsc(Long tenantId);
    
    // Statistics
    long countByTenantIdAndIsActiveTrue(Long tenantId);
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
}