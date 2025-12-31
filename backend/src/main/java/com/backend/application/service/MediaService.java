package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.entity.Media;

/**
 * Media service interface for managing media files.
 * Simplified for Phase 1 - Core CRUD operations only.
 * Additional methods will be added in Phase 2.
 */
public interface MediaService {

    // Basic CRUD operations
    Media uploadFile(MultipartFile file, Long uploadedBy);

    Optional<Media> findById(Long id);

    Optional<Media> findByUid(String uid);

    Optional<Media> findByFileName(String fileName);

    Media update(Media media);

    void delete(Long id);

    List<Media> findAll();

    // File retrieval
    byte[] getFileContent(String fileName);

    String getFileUrl(Long id);

    // Folder operations
    List<Media> findByFolderId(Long folderId);

    // Statistics
    long count();

    long sumFileSize();

    // Validation
    boolean isValidFileType(MultipartFile file);

    boolean isFileSizeAllowed(MultipartFile file, Long maxSize);

    List<String> getAllowedExtensions();
}