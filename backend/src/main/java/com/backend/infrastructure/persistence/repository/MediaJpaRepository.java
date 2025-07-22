package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaJpaRepository extends JpaRepository<MediaFile, Long> {
    
    // File queries
    Optional<MediaFile> findByFileName(String fileName);
    boolean existsByFileName(String fileName);
    boolean existsByFilePath(String filePath);
    
    // Tenant-specific queries
    List<MediaFile> findByTenantId(Long tenantId);
    List<MediaFile> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    
    // Uploader queries
    List<MediaFile> findByUploadedBy(Long uploadedBy);
    List<MediaFile> findByTenantIdAndUploadedBy(Long tenantId, Long uploadedBy);
    long countByTenantIdAndUploadedBy(Long tenantId, Long uploadedBy);
    
    // File type queries
    List<MediaFile> findByMimeType(String mimeType);
    List<MediaFile> findByTenantIdAndMimeType(Long tenantId, String mimeType);
    
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND m.mimeType LIKE :mimeTypePrefix")
    List<MediaFile> findByTenantIdAndMimeTypeStartingWith(@Param("tenantId") Long tenantId, 
                                                        @Param("mimeTypePrefix") String mimeTypePrefix);
    
    // Folder and category queries
    List<MediaFile> findByFolder(String folder);
    List<MediaFile> findByTenantIdAndFolder(Long tenantId, String folder);
    List<MediaFile> findByCategory(String category);
    List<MediaFile> findByTenantIdAndCategory(Long tenantId, String category);
    List<MediaFile> findByTenantIdAndFolderAndCategory(Long tenantId, String folder, String category);
    
    // File size queries
    List<MediaFile> findByFileSizeBetween(Long minSize, Long maxSize);
    List<MediaFile> findByTenantIdAndFileSizeBetween(Long tenantId, Long minSize, Long maxSize);
    List<MediaFile> findByFileSizeGreaterThan(Long size);
    List<MediaFile> findByFileSizeLessThan(Long size);
    
    // Image dimension queries
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND m.width IS NOT NULL AND m.height IS NOT NULL")
    List<MediaFile> findImagesByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND m.width >= :minWidth AND m.height >= :minHeight")
    List<MediaFile> findByTenantIdAndMinDimensions(@Param("tenantId") Long tenantId, 
                                                 @Param("minWidth") Integer minWidth, 
                                                 @Param("minHeight") Integer minHeight);
    
    // Access and visibility queries
    List<MediaFile> findByIsPublic(Boolean isPublic);
    List<MediaFile> findByTenantIdAndIsPublic(Long tenantId, Boolean isPublic);
    List<MediaFile> findByIsOptimized(Boolean isOptimized);
    List<MediaFile> findByTenantIdAndIsOptimized(Long tenantId, Boolean isOptimized);
    
    // Usage queries
    List<MediaFile> findByUsageCountGreaterThan(Integer usageCount);
    List<MediaFile> findByTenantIdAndUsageCountGreaterThan(Long tenantId, Integer usageCount);
    List<MediaFile> findByUsageCount(Integer usageCount);
    
    // Storage provider queries
    List<MediaFile> findByStorageProvider(String storageProvider);
    List<MediaFile> findByTenantIdAndStorageProvider(Long tenantId, String storageProvider);
    List<MediaFile> findByExternalUrlIsNotNull();
    List<MediaFile> findByTenantIdAndExternalUrlIsNotNull(Long tenantId);
    
    // Search queries
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND " +
           "(LOWER(m.originalName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.altTextTr) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.altTextEn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.titleTr) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.titleEn) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<MediaFile> findByTenantIdAndSearchTerm(@Param("tenantId") Long tenantId, 
                                              @Param("searchTerm") String searchTerm);
    
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND " +
           "LOWER(m.originalName) LIKE LOWER(CONCAT('%', :filename, '%'))")
    List<MediaFile> findByTenantIdAndOriginalNameContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                                    @Param("filename") String filename);
    
    // Date range queries
    List<MediaFile> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByLastAccessedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByTenantIdAndLastAccessedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Recent files
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    List<MediaFile> findRecentByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId AND m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<MediaFile> findRecentByTenantIdSince(@Param("tenantId") Long tenantId, 
                                            @Param("since") LocalDateTime since);
    
    // Most used files
    @Query("SELECT m FROM MediaFile m WHERE m.tenantId = :tenantId ORDER BY m.usageCount DESC")
    List<MediaFile> findMostUsedByTenantId(@Param("tenantId") Long tenantId);
    
    // Bulk operations
    List<MediaFile> findByIdIn(List<Long> ids);
    void deleteByTenantId(Long tenantId);
    void deleteByUploadedBy(Long uploadedBy);
    
    // Statistics
    long countByTenantIdAndMimeTypeStartingWith(Long tenantId, String mimeTypePrefix);
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndFolder(Long tenantId, String folder);
    long countByTenantIdAndCategory(Long tenantId, String category);
    
    @Query("SELECT SUM(m.fileSize) FROM MediaFile m WHERE m.tenantId = :tenantId")
    Long sumFileSizeByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT SUM(m.fileSize) FROM MediaFile m WHERE m.tenantId = :tenantId AND m.folder = :folder")
    Long sumFileSizeByTenantIdAndFolder(@Param("tenantId") Long tenantId, 
                                      @Param("folder") String folder);
}