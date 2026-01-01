package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.entity.Media;

public interface MediaService {

    // Basic CRUD operations
    Media uploadFile(MultipartFile file, Long uploadedBy);

    Optional<Media> findById(Long id);

    Optional<Media> findByUid(String uid);

    Optional<Media> findByFileName(String fileName);

    Media update(Media media);

    /**
     * Update media metadata.
     *
     * @param id       media ID
     * @param folderId new folder ID (nullable)
     * @param isPublic public access flag
     * @param tags     tags list
     * @return updated Media
     */
    Media updateMetadata(Long id, Long folderId, Boolean isPublic, List<String> tags);

    void delete(Long id);

    List<Media> findAll();

    /**
     * Find all media with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated media list
     */
    Page<Media> findAll(Pageable pageable);

    // File retrieval
    byte[] getFileContent(String fileName);

    String getFileUrl(Long id);

    // Folder operations
    List<Media> findByFolderId(Long folderId);

    /**
     * Move media to a different folder.
     *
     * @param mediaId  media ID
     * @param folderId target folder ID (nullable for root)
     * @return updated Media
     */
    Media moveToFolder(Long mediaId, Long folderId);

    // Statistics
    long count();

    long sumFileSize();

    // Validation
    boolean isValidFileType(MultipartFile file);

    boolean isFileSizeAllowed(MultipartFile file, Long maxSize);

    List<String> getAllowedExtensions();

    /**
     * Find media by multiple UIDs (batch query).
     * Used by CMS delivery API for efficient batch lookups.
     *
     * @param uids list of media UIDs
     * @return list of Media matching the UIDs
     */
    List<Media> findByUids(List<String> uids);
}