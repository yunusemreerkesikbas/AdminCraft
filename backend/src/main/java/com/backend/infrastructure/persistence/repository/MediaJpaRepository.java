package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;

@Repository
public interface MediaJpaRepository extends JpaRepository<Media, Long> {

    // UUID/UID queries
    Optional<Media> findByUid(String uid);

    Optional<Media> findByUuid(String uuid);

    boolean existsByUid(String uid);

    // File identification queries
    Optional<Media> findByFileName(String fileName);

    boolean existsByFileName(String fileName);

    // Folder queries
    @EntityGraph(attributePaths = { "folder", "translations" })
    List<Media> findByFolderId(Long folderId);

    @EntityGraph(attributePaths = { "folder", "translations" })
    List<Media> findByFolderIsNull();

    long countByFolderId(Long folderId);

    // File type queries
    @Query("SELECT m FROM Media m WHERE m.mimeType LIKE CONCAT(:prefix, '%')")
    List<Media> findByMimeTypeStartingWith(@Param("prefix") String mimeTypePrefix);

    List<Media> findByFileExtension(String extension);

    List<Media> findByFileExtensionIn(List<String> extensions);

    // Image-specific queries
    List<Media> findByWidthIsNotNull();

    List<Media> findByWidthBetween(Integer minWidth, Integer maxWidth);

    List<Media> findByHeightBetween(Integer minHeight, Integer maxHeight);

    // File size queries
    List<Media> findByFileSizeBetween(Long minSize, Long maxSize);

    List<Media> findByFileSizeGreaterThan(Long size);

    List<Media> findAllByOrderByFileSizeDesc();

    // Paginated queries
    Page<Media> findAllByOrderByFileSizeDesc(Pageable pageable);

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

    List<Media> findAllByOrderByUsageCountDesc();

    // Date queries
    List<Media> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Media> findByLastAccessedAtBefore(LocalDateTime dateTime);

    List<Media> findAllByOrderByCreatedAtDesc();

    // Bulk operations
    List<Media> findByIdIn(List<Long> ids);

    List<Media> findByUidIn(List<String> uids);

    List<Media> findByFileNameIn(List<String> fileNames);

    @Modifying
    void deleteByUploadedBy(Long userId);

    // Custom queries
    @Query("SELECT m FROM Media m WHERE m.usageCount = 0")
    List<Media> findUnusedMedia();

    @Query("SELECT SUM(m.fileSize) FROM Media m")
    Long sumFileSize();

    @Query("SELECT COUNT(m) FROM Media m WHERE m.mimeType LIKE CONCAT(:prefix, '%')")
    long countByMimeTypeStartingWith(@Param("prefix") String mimeTypePrefix);
}