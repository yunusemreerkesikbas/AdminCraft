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

    // Uploader queries
    List<MediaFile> findByUploadedBy(Long uploadedBy);

    // File type queries
    List<MediaFile> findByMimeType(String mimeType);

    @Query("SELECT m FROM MediaFile m WHERE m.mimeType LIKE :mimeTypePrefix")
    List<MediaFile> findByMimeTypeStartingWith(@Param("mimeTypePrefix") String mimeTypePrefix);

    // Folder and category queries
    List<MediaFile> findByFolder(String folder);
    List<MediaFile> findByCategory(String category);

    // File size queries
    List<MediaFile> findByFileSizeBetween(Long minSize, Long maxSize);
    List<MediaFile> findByFileSizeGreaterThan(Long size);
    List<MediaFile> findByFileSizeLessThan(Long size);

    // Access and visibility queries
    List<MediaFile> findByIsPublic(Boolean isPublic);
    List<MediaFile> findByIsOptimized(Boolean isOptimized);

    // Usage queries
    List<MediaFile> findByUsageCountGreaterThan(Integer usageCount);
    List<MediaFile> findByUsageCount(Integer usageCount);

    // Storage provider queries
    List<MediaFile> findByStorageProvider(String storageProvider);
    List<MediaFile> findByExternalUrlIsNotNull();

    // Date range queries
    List<MediaFile> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<MediaFile> findByLastAccessedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Bulk operations
    List<MediaFile> findByIdIn(List<Long> ids);
    void deleteByUploadedBy(Long uploadedBy);
}