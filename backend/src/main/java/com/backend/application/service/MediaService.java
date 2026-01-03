package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.entity.Media;

public interface MediaService {

    Media uploadFile(MultipartFile file, Long uploadedBy);

    /**
     * Upload a file with optional i18n data in a single transaction.
     *
     * @param file         The file to upload
     * @param uploadedBy   User ID
     * @param translations Map of language to i18n request (optional)
     * @return Created media
     */
    Media uploadComposite(MultipartFile file, Long uploadedBy,
            java.util.Map<com.backend.domain.enums.Language, com.backend.presentation.dto.request.MediaI18nRequest> translations);

    Optional<Media> findById(Long id);

    Optional<Media> findByUid(String uid);

    Optional<Media> findByFileName(String fileName);

    Media update(Media media);

    /**
     * Update media metadata.
     *
     * @param id       media ID
     * @param isPublic public access flag
     * @param tags     tags list
     * @return updated Media
     */
    Media updateMetadata(Long id, Boolean isPublic, List<String> tags);

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

    /**
     * Update focal point for smart cropping.
     *
     * @param id media ID
     * @param x  horizontal focal point (0.0 = left, 1.0 = right)
     * @param y  vertical focal point (0.0 = top, 1.0 = bottom)
     */
    void updateFocalPoint(Long id, Double x, Double y);
}
