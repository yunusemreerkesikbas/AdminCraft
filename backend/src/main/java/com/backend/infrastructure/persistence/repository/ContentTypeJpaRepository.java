package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentTypeJpaRepository extends JpaRepository<ContentType, Long> {
    
    // Basic tenant queries
    List<ContentType> findByTenantId(Long tenantId);
    List<ContentType> findByTenantIdOrderBySortOrderAsc(Long tenantId);
    List<ContentType> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    
    // Name queries
    Optional<ContentType> findByName(String name);
    Optional<ContentType> findByTenantIdAndName(Long tenantId, String name);
    boolean existsByName(String name);
    boolean existsByTenantIdAndName(Long tenantId, String name);
    
    // Display name queries
    List<ContentType> findByDisplayName(String displayName);
    List<ContentType> findByTenantIdAndDisplayName(Long tenantId, String displayName);
    
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND " +
           "(LOWER(ct.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.displayNameTr) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.displayNameEn) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ContentType> findByTenantIdAndDisplayNameContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                                     @Param("searchTerm") String searchTerm);
    
    // System type queries
    List<ContentType> findByIsSystemType(Boolean isSystemType);
    List<ContentType> findByTenantIdAndIsSystemType(Long tenantId, Boolean isSystemType);
    List<ContentType> findByTenantIdAndIsSystemTypeOrderBySortOrderAsc(Long tenantId, Boolean isSystemType);
    
    // Active status queries
    List<ContentType> findByIsActive(Boolean isActive);
    List<ContentType> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    List<ContentType> findByTenantIdAndIsActiveOrderBySortOrderAsc(Long tenantId, Boolean isActive);
    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    
    // Multi-language support queries
    List<ContentType> findBySupportsMultiLanguage(Boolean supportsMultiLanguage);
    List<ContentType> findByTenantIdAndSupportsMultiLanguage(Long tenantId, Boolean supportsMultiLanguage);
    
    // Feature support queries
    List<ContentType> findBySupportsSeo(Boolean supportsSeo);
    List<ContentType> findByTenantIdAndSupportsSeo(Long tenantId, Boolean supportsSeo);
    
    List<ContentType> findBySupportsScheduling(Boolean supportsScheduling);
    List<ContentType> findByTenantIdAndSupportsScheduling(Long tenantId, Boolean supportsScheduling);
    
    List<ContentType> findBySupportsComments(Boolean supportsComments);
    List<ContentType> findByTenantIdAndSupportsComments(Long tenantId, Boolean supportsComments);
    
    List<ContentType> findByRequiresApproval(Boolean requiresApproval);
    List<ContentType> findByTenantIdAndRequiresApproval(Long tenantId, Boolean requiresApproval);
    
    // Author queries
    List<ContentType> findByCreatedBy(Long createdBy);
    List<ContentType> findByTenantIdAndCreatedBy(Long tenantId, Long createdBy);
    List<ContentType> findByUpdatedBy(Long updatedBy);
    List<ContentType> findByTenantIdAndUpdatedBy(Long tenantId, Long updatedBy);
    
    // Date range queries
    List<ContentType> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ContentType> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<ContentType> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ContentType> findByTenantIdAndUpdatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Icon and color queries
    List<ContentType> findByIcon(String icon);
    List<ContentType> findByTenantIdAndIcon(Long tenantId, String icon);
    List<ContentType> findByColor(String color);
    List<ContentType> findByTenantIdAndColor(Long tenantId, String color);
    
    // Max items queries
    List<ContentType> findByMaxItemsIsNull(); // Unlimited items
    List<ContentType> findByTenantIdAndMaxItemsIsNull(Long tenantId); // Unlimited items
    List<ContentType> findByMaxItemsIsNotNull(); // Limited items
    List<ContentType> findByTenantIdAndMaxItemsIsNotNull(Long tenantId); // Limited items
    List<ContentType> findByMaxItemsGreaterThan(Integer maxItems);
    List<ContentType> findByTenantIdAndMaxItemsGreaterThan(Long tenantId, Integer maxItems);
    
    // Search queries
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND " +
           "(LOWER(ct.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ContentType> findByTenantIdAndSearchTerm(@Param("tenantId") Long tenantId, 
                                                @Param("searchTerm") String searchTerm);
    
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND " +
           "LOWER(ct.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ContentType> findByTenantIdAndNameContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                              @Param("name") String name);
    
    // Complex queries
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND ct.isActive = true AND " +
           "(ct.isSystemType = :includeSystemTypes OR ct.isSystemType = false) " +
           "ORDER BY ct.sortOrder ASC, ct.displayName ASC")
    List<ContentType> findActiveContentTypesByTenantId(@Param("tenantId") Long tenantId, 
                                                     @Param("includeSystemTypes") Boolean includeSystemTypes);
    
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND ct.isActive = true AND " +
           "ct.supportsMultiLanguage = :supportsMultiLanguage " +
           "ORDER BY ct.sortOrder ASC")
    List<ContentType> findActiveByTenantIdAndMultiLanguageSupport(@Param("tenantId") Long tenantId, 
                                                                @Param("supportsMultiLanguage") Boolean supportsMultiLanguage);
    
    // Bulk operations
    List<ContentType> findByIdIn(List<Long> ids);
    void deleteByTenantId(Long tenantId);
    void deleteByCreatedBy(Long createdBy);
    
    // Statistics
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndIsSystemType(Long tenantId, Boolean isSystemType);
    long countByTenantIdAndSupportsMultiLanguage(Long tenantId, Boolean supportsMultiLanguage);
    long countByTenantIdAndSupportsSeo(Long tenantId, Boolean supportsSeo);
    
    // Content count per content type (requires join with Content entity)
    @Query("SELECT ct.id, ct.name, COUNT(c.id) FROM ContentType ct " +
           "LEFT JOIN Content c ON c.contentTypeId = ct.id " +
           "WHERE ct.tenantId = :tenantId " +
           "GROUP BY ct.id, ct.name " +
           "ORDER BY COUNT(c.id) DESC")
    List<Object[]> getContentCountsByContentType(@Param("tenantId") Long tenantId);
    
    // Recent content types
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId ORDER BY ct.createdAt DESC")
    List<ContentType> findRecentByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT ct FROM ContentType ct WHERE ct.tenantId = :tenantId AND ct.createdAt >= :since ORDER BY ct.createdAt DESC")
    List<ContentType> findRecentByTenantIdSince(@Param("tenantId") Long tenantId, 
                                              @Param("since") LocalDateTime since);
}