package com.backend.application.service;

import com.backend.domain.entity.MediaFile;
import com.backend.domain.enums.Language;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MediaService {
    
    // Basic CRUD operations
    MediaFile uploadFile(MultipartFile file, Long tenantId, Long uploadedBy);
    Optional<MediaFile> getMediaFileById(Long id);
    MediaFile updateMediaFile(MediaFile mediaFile);
    void deleteMediaFile(Long id);
    // REMOVED: getAllMediaFiles() - SECURITY RISK: Must be tenant-scoped
    // Use getMediaFilesByTenantId(Long tenantId) instead
    
    // File retrieval operations
    Optional<MediaFile> getMediaFileByFileName(String fileName);
    byte[] getFileContent(String fileName);
    byte[] getThumbnailContent(String fileName);
    String getFileUrl(Long id);
    String getThumbnailUrl(Long id);
    
    // Tenant-specific operations
    List<MediaFile> getMediaFilesByTenantId(Long tenantId);
    List<MediaFile> getRecentMediaFiles(Long tenantId, int limit);
    long countMediaFilesByTenantId(Long tenantId);
    long getTotalStorageUsed(Long tenantId);
    
    // File type operations
    List<MediaFile> getImageFiles(Long tenantId);
    List<MediaFile> getVideoFiles(Long tenantId);
    List<MediaFile> getAudioFiles(Long tenantId);
    List<MediaFile> getDocumentFiles(Long tenantId);
    List<MediaFile> getFilesByMimeType(Long tenantId, String mimeType);
    List<MediaFile> getFilesByExtension(Long tenantId, String extension);
    
    // Organization operations
    List<MediaFile> getFilesByFolder(Long tenantId, String folder);
    List<MediaFile> getFilesByCategory(Long tenantId, String category);
    List<String> getAllFolders(Long tenantId);
    List<String> getAllCategories(Long tenantId);
    MediaFile moveToFolder(Long id, String folder);
    MediaFile changeCategory(Long id, String category);
    
    // Multi-language operations
    MediaFile updateAltText(Long id, Language language, String altText);
    MediaFile updateDescription(Long id, Language language, String description);
    MediaFile updateTitle(Long id, Language language, String title);
    String getLocalizedAltText(Long id, Language language);
    String getLocalizedDescription(Long id, Language language);
    String getLocalizedTitle(Long id, Language language);
    
    // Search operations
    List<MediaFile> searchByName(Long tenantId, String searchTerm);
    List<MediaFile> searchByAltText(Long tenantId, String searchTerm, Language language);
    List<MediaFile> searchByDescription(Long tenantId, String searchTerm, Language language);
    List<MediaFile> searchByTags(Long tenantId, String tag);
    
    // File size and dimension operations
    List<MediaFile> getFilesBySize(Long tenantId, Long minSize, Long maxSize);
    List<MediaFile> getLargeFiles(Long tenantId, Long minSize);
    List<MediaFile> getImagesByDimensions(Long tenantId, Integer minWidth, Integer minHeight);
    List<MediaFile> getLargestFiles(Long tenantId, int limit);
    
    // Image-specific operations
    MediaFile generateThumbnails(Long id);
    MediaFile optimizeImage(Long id);
    List<MediaFile> getUnoptimizedImages(Long tenantId);
    MediaFile resizeImage(Long id, Integer width, Integer height);
    
    // Usage tracking operations
    void incrementUsage(Long id);
    List<MediaFile> getMostUsedFiles(Long tenantId, int limit);
    List<MediaFile> getUnusedFiles(Long tenantId);
    List<MediaFile> getFilesByUsageCount(Long tenantId, int minUsageCount);
    
    // Access control operations
    List<MediaFile> getPublicFiles(Long tenantId);
    List<MediaFile> getPrivateFiles(Long tenantId);
    MediaFile makeFilePublic(Long id);
    MediaFile makeFilePrivate(Long id);
    
    // Storage provider operations
    List<MediaFile> getFilesByStorageProvider(Long tenantId, String storageProvider);
    MediaFile migrateToStorageProvider(Long id, String newProvider);
    void syncWithExternalStorage(Long tenantId);
    
    // Date range operations
    List<MediaFile> getFilesUploadedBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> getRecentlyAccessedFiles(Long tenantId, LocalDateTime since);
    List<MediaFile> getOldFiles(Long tenantId, LocalDateTime before);
    
    // Bulk operations
    void bulkDelete(List<Long> fileIds);
    void bulkMoveToFolder(List<Long> fileIds, String folder);
    void bulkChangeCategory(List<Long> fileIds, String category);
    void bulkMakePublic(List<Long> fileIds);
    void bulkMakePrivate(List<Long> fileIds);
    void bulkOptimize(List<Long> fileIds);
    void deleteFilesByTenantId(Long tenantId);
    
    // File validation operations
    boolean isValidFileType(MultipartFile file);
    boolean isFileSizeAllowed(MultipartFile file, Long maxSize);
    List<String> getAllowedExtensions();
    List<String> getAllowedMimeTypes();
    
    // Duplicate detection
    List<MediaFile> findDuplicateFiles(Long tenantId);
    Optional<MediaFile> findDuplicateByHash(String fileHash, Long tenantId);
    MediaFile checkForDuplicate(MultipartFile file, Long tenantId);
    
    // Statistics operations - ALL TENANT SCOPED
    long getTotalFilesCount(Long tenantId); // Sprint 7: Tenant scoped
    long getImageFilesCount(Long tenantId);
    long getVideoFilesCount(Long tenantId);
    long getDocumentFilesCount(Long tenantId);
    long getFilesCountByExtension(Long tenantId, String extension);
    
    // Cleanup operations
    List<MediaFile> getOrphanedFiles(Long tenantId);
    void cleanupUnusedFiles(Long tenantId, int unusedDays);
    void cleanupThumbnails(Long tenantId);
    void validateFileIntegrity(Long tenantId);
    
    // Metadata operations
    MediaFile extractMetadata(Long id);
    MediaFile updateMetadata(Long id, String metadata);
    String getFileMetadata(Long id);
    
    // Export and backup operations
    List<MediaFile> exportMediaList(Long tenantId);
    void backupFiles(Long tenantId, String backupPath);
    void restoreFiles(Long tenantId, String backupPath);
}