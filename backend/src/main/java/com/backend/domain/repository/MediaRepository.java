package com.backend.domain.repository;

import com.backend.domain.entity.MediaFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MediaRepository {
    
    // Basic CRUD operations
    MediaFile save(MediaFile mediaFile);
    Optional<MediaFile> findById(Long id);
    List<MediaFile> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    
    // File identification queries
    Optional<MediaFile> findByFileName(String fileName);
    boolean existsByFileName(String fileName);
    boolean existsByFilePath(String filePath);
    
    // Tenant-specific queries
    List<MediaFile> findByTenantId(Long tenantId);
    List<MediaFile> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    long sumFileSizeByTenantId(Long tenantId);
    
    // File type queries
    List<MediaFile> findByMimeTypeStartingWith(String mimeTypePrefix);
    List<MediaFile> findByTenantIdAndMimeTypeStartingWith(Long tenantId, String mimeTypePrefix);
    List<MediaFile> findByTenantIdAndFileExtension(Long tenantId, String extension);
    List<MediaFile> findByTenantIdAndFileExtensionIn(Long tenantId, List<String> extensions);
    
    // Image-specific queries
    List<MediaFile> findByTenantIdAndWidthIsNotNull(Long tenantId); // Images only
    List<MediaFile> findByTenantIdAndHeightIsNotNull(Long tenantId); // Images only
    List<MediaFile> findByTenantIdAndWidthBetween(Long tenantId, Integer minWidth, Integer maxWidth);
    List<MediaFile> findByTenantIdAndHeightBetween(Long tenantId, Integer minHeight, Integer maxHeight);
    List<MediaFile> findByTenantIdAndHasThumbnailsTrue(Long tenantId);
    
    // File size queries
    List<MediaFile> findByFileSizeBetween(Long minSize, Long maxSize);
    List<MediaFile> findByTenantIdAndFileSizeBetween(Long tenantId, Long minSize, Long maxSize);
    List<MediaFile> findByTenantIdAndFileSizeGreaterThan(Long tenantId, Long size);
    List<MediaFile> findByTenantIdOrderByFileSizeDesc(Long tenantId);
    
    // Organization queries
    List<MediaFile> findByTenantIdAndFolder(Long tenantId, String folder);
    List<MediaFile> findByTenantIdAndCategory(Long tenantId, String category);
    List<MediaFile> findByTenantIdAndFolderAndCategory(Long tenantId, String folder, String category);
    List<String> findDistinctFolderByTenantId(Long tenantId);
    List<String> findDistinctCategoryByTenantId(Long tenantId);
    
    // Access control queries
    List<MediaFile> findByIsPublicTrue();
    List<MediaFile> findByTenantIdAndIsPublicTrue(Long tenantId);
    List<MediaFile> findByTenantIdAndIsPublicFalse(Long tenantId);
    
    // Storage queries
    List<MediaFile> findByStorageProvider(String storageProvider);
    List<MediaFile> findByTenantIdAndStorageProvider(Long tenantId, String storageProvider);
    List<MediaFile> findByExternalIdIsNotNull();
    List<MediaFile> findByTenantIdAndExternalIdIsNotNull(Long tenantId);
    
    // Uploader queries
    List<MediaFile> findByUploadedBy(Long userId);
    List<MediaFile> findByTenantIdAndUploadedBy(Long tenantId, Long userId);
    long countByTenantIdAndUploadedBy(Long tenantId, Long userId);
    
    // Search queries
    List<MediaFile> findByTenantIdAndOriginalNameContainingIgnoreCase(Long tenantId, String originalName);
    List<MediaFile> findByTenantIdAndAltTextTrContainingIgnoreCase(Long tenantId, String altText);
    List<MediaFile> findByTenantIdAndAltTextEnContainingIgnoreCase(Long tenantId, String altText);
    List<MediaFile> findByTenantIdAndDescriptionTrContainingIgnoreCase(Long tenantId, String description);
    List<MediaFile> findByTenantIdAndDescriptionEnContainingIgnoreCase(Long tenantId, String description);
    
    // Usage queries
    List<MediaFile> findByUsageCountGreaterThan(Integer count);
    List<MediaFile> findByTenantIdAndUsageCountGreaterThan(Long tenantId, Integer count);
    List<MediaFile> findByTenantIdAndUsageCount(Long tenantId, Integer count);
    List<MediaFile> findByTenantIdOrderByUsageCountDesc(Long tenantId);
    
    // Date queries
    List<MediaFile> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByLastAccessedAtBefore(LocalDateTime dateTime);
    List<MediaFile> findByTenantIdAndLastAccessedAtBefore(Long tenantId, LocalDateTime dateTime);
    
    // Optimization queries
    List<MediaFile> findByIsOptimizedTrue();
    List<MediaFile> findByTenantIdAndIsOptimizedTrue(Long tenantId);
    List<MediaFile> findByTenantIdAndIsOptimizedFalse(Long tenantId);
    
    // Metadata queries
    List<MediaFile> findByMetadataIsNotNull();
    List<MediaFile> findByTenantIdAndMetadataIsNotNull(Long tenantId);
    
    // Bulk operations
    List<MediaFile> findByIdIn(List<Long> ids);
    List<MediaFile> findByFileNameIn(List<String> fileNames);
    void deleteByTenantId(Long tenantId);
    void deleteByUploadedBy(Long userId);
    
    // Custom queries for file management
    List<MediaFile> findUnusedFiles(Long tenantId); // Files with usage count 0
    List<MediaFile> findOrphanedFiles(Long tenantId, LocalDateTime olderThan);
    List<MediaFile> findLargestFiles(Long tenantId, int limit);
    List<MediaFile> findDuplicateFiles(Long tenantId); // Files with same name/size
    
    // Statistics
    long countByTenantIdAndMimeTypeStartingWith(Long tenantId, String mimeTypePrefix);
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndStorageProvider(Long tenantId, String storageProvider);
    
    // Recent and popular content
    List<MediaFile> findRecentlyUploaded(Long tenantId, int limit);
    List<MediaFile> findMostUsed(Long tenantId, int limit);
    List<MediaFile> findRecentlyAccessed(Long tenantId, int limit);
}