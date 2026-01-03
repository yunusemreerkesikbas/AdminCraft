package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.dto.ImageDimensions;
import com.backend.domain.entity.Media;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaRepository;
import com.backend.presentation.dto.request.MediaI18nRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaI18nService i18nService;
    private final MediaStorageService storageService;
    private final MediaProcessingService processingService;
    private final MediaContainerService containerService;
    private final StorageConfigProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Media uploadComposite(MultipartFile file, Long uploadedBy,
            Map<Language, MediaI18nRequest> translations) {
        // 1. Upload basic file (reuses existing logic)
        Media media = uploadFile(file, uploadedBy);

        return transactionTemplate.execute(status -> {
            Media currentMedia = mediaRepository.findById(media.getId()).orElseThrow();

            Media saved = mediaRepository.save(currentMedia);

            // 2. Create I18n entries
            if (translations != null && !translations.isEmpty()) {
                translations.forEach((lang, req) -> {
                    i18nService.upsert(saved.getId(), lang, req.altText(), req.title(), req.description());
                });
            }
            return saved;
        });
    }

    @Override
    public Media uploadFile(MultipartFile file, Long uploadedBy) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        MediaStorageService.ValidationResult validation = storageService.validate(file);
        if (!validation.valid()) {
            throw new IllegalArgumentException(String.join(", ", validation.errors()));
        }

        // I/O Operation 1: Store file (Outside Transaction)
        MediaStorageService.StoredFileResult stored = storageService.store(file, "media");

        // I/O Operation 2: Extract dimensions BEFORE transaction (avoid holding DB
        // connection)
        ImageDimensions dimensions = null;
        boolean requiresProcessing = processingService.isProcessingSupported(stored.mimeType());
        if (requiresProcessing) {
            byte[] content = storageService.retrieve(stored.filePath());
            dimensions = processingService.extractDimensions(content);
        }

        final ImageDimensions finalDimensions = dimensions;

        try {
            // Database Operations (Transactional via TransactionTemplate)
            Media savedMedia = transactionTemplate.execute(status -> {
                Media media = new Media();
                media.setOriginalName(file.getOriginalFilename());
                media.setFileName(stored.fileName());
                media.setFilePath(stored.filePath());
                media.setMimeType(stored.mimeType());
                media.setFileSize(stored.fileSize());
                media.setFileExtension(stored.extension());
                media.setUploadedBy(uploadedBy);
                media.setStorageProvider(StorageProvider.valueOf(properties.getProvider().toUpperCase()));
                media.setIsPublic(true);
                media.setUsageCount(0);

                if (requiresProcessing) {
                    media.setStatus(MediaStatus.PROCESSING);
                    if (finalDimensions != null) {
                        media.setWidth(finalDimensions.width());
                        media.setHeight(finalDimensions.height());
                    }
                } else {
                    media.setStatus(MediaStatus.ACTIVE);
                }

                Media saved = mediaRepository.save(media);
                log.info("File uploaded: {} with UID: {}", stored.fileName(), saved.getUid());
                containerService.createForMedia(saved.getId());
                return saved;
            });

            if (savedMedia != null && requiresProcessing) {
                processingService.generateFormats(savedMedia.getId());
            }

            return savedMedia;
        } catch (Exception e) {
            log.error("Failed to save media metadata, rolling back file storage: {}", stored.filePath());
            storageService.delete(stored.filePath());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> findById(Long id) {
        return mediaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> findByUid(String uid) {
        return mediaRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> findByFileName(String fileName) {
        return mediaRepository.findByFileName(fileName);
    }

    @Override
    @Transactional
    public Media update(Media media) {
        log.debug("Updating media with UID: {}", media.getUid());

        if (!mediaRepository.existsById(media.getId())) {
            throw new IllegalArgumentException("Media not found with ID: " + media.getId());
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media updated: {}", updatedMedia.getUid());
        return updatedMedia;
    }

    @Override
    @Transactional
    public Media updateMetadata(Long id, Boolean isPublic, List<String> tags) {
        log.debug("Updating media metadata for ID: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        // Update public flag if provided
        if (isPublic != null) {
            media.setIsPublic(isPublic);
        }

        // Update tags if provided
        if (tags != null) {
            media.setTags(String.join(",", tags));
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media metadata updated: {}", updatedMedia.getUid());
        return updatedMedia;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting media with ID: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        String filePath = media.getFilePath();

        // Delete DB record first (inside transaction)
        mediaRepository.deleteById(id);
        log.info("Media deleted from database: {}", id);

        // Delete file after DB commit (best effort)
        try {
            storageService.delete(filePath);
        } catch (Exception e) {
            log.warn("Failed to delete file {} after DB deletion: {}", filePath, e.getMessage());
            // Consider adding to a cleanup queue for retry
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> findAll() {
        return mediaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Media> findAll(Pageable pageable) {
        return mediaRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public byte[] getFileContent(String fileName) {
        Media media = mediaRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        media.recordAccess();
        mediaRepository.save(media);

        return storageService.retrieve(media.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String getFileUrl(Long id) {
        return "/api/media/files/" + id;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return mediaRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long sumFileSize() {
        return mediaRepository.sumFileSize();
    }

    @Override
    public boolean isValidFileType(MultipartFile file) {
        return storageService.isValidMimeType(file.getContentType());
    }

    @Override
    public boolean isFileSizeAllowed(MultipartFile file, Long maxSize) {
        return storageService.isValidFileSize(file.getSize());
    }

    @Override
    public List<String> getAllowedExtensions() {
        return properties.getAllowedMimeTypes().stream()
                .map(mime -> mime.substring(mime.lastIndexOf('/') + 1))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> findByUids(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return List.of();
        }
        return mediaRepository.findByUidIn(uids);
    }
}
