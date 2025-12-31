package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;

/**
 * Repository interface for Media entity.
 * Refactored from legacy MediaFile repository.
 */
public interface MediaRepository {

    // Basic CRUD operations
    Media save(Media media);

    Optional<Media> findById(Long id);

    Optional<Media> findByUid(String uid);

    Optional<Media> findByUuid(String uuid);

    List<Media> findAll();

    void deleteById(Long id);

    void delete(Media media);

    boolean existsById(Long id);

    boolean existsByUid(String uid);

    long count();

    // File identification queries
    Optional<Media> findByFileName(String fileName);

    boolean existsByFileName(String fileName);

    // Folder queries
    List<Media> findByFolderId(Long folderId);

    List<Media> findByFolderIsNull();

    long countByFolderId(Long folderId);

    // File type queries
    List<Media> findByMimeTypeStartingWith(String mimeTypePrefix);

    List<Media> findByFileExtension(String extension);

    List<Media> findByFileExtensionIn(List<String> extensions);

    // Image-specific queries
    List<Media> findByWidthIsNotNull();

    List<Media> findByWidthBetween(Integer minWidth, Integer maxWidth);

    List<Media> findByHeightBetween(Integer minHeight, Integer maxHeight);

    // File size queries
    List<Media> findByFileSizeBetween(Long minSize, Long maxSize);

    List<Media> findByFileSizeGreaterThan(Long size);

    List<Media> findAllOrderByFileSizeDesc();

    // Status queries
    List<Media> findByStatus(MediaStatus status);

    long countByStatus(MediaStatus status);

    // Access control queries
    List<Media> findByIsPublicTrue();

    List<Media> findByIsPublicFalse();

    // Storage queries
    List<Media> findByStorageProvider(StorageProvider storageProvider);

    List<Media> findByExternalIdIsNotNull();

    // Uploader queries
    List<Media> findByUploadedBy(Long userId);

    long countByUploadedBy(Long userId);

    // Search queries
    List<Media> findByOriginalNameContainingIgnoreCase(String originalName);

    // Usage queries
    List<Media> findByUsageCountGreaterThan(Integer count);

    List<Media> findByUsageCount(Integer count);

    List<Media> findAllOrderByUsageCountDesc();

    // Date queries
    List<Media> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Media> findByLastAccessedAtBefore(LocalDateTime dateTime);

    List<Media> findAllOrderByCreatedAtDesc();

    // Bulk operations
    List<Media> findByIdIn(List<Long> ids);

    List<Media> findByUidIn(List<String> uids);

    List<Media> findByFileNameIn(List<String> fileNames);

    void deleteByUploadedBy(Long userId);

    // Custom queries
    List<Media> findUnusedMedia(); // Files with usage count 0

    List<Media> findLargestMedia(int limit);

    // Statistics
    long countByMimeTypeStartingWith(String mimeTypePrefix);

    long sumFileSize();
}