package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.command.MediaProcessingCommands.ImageDimensions;
import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaRepository;
import com.backend.infrastructure.storage.StorageProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaStorageService storageService;
    private final MediaProcessingService processingService;
    private final MediaContainerService containerService;
    private final StorageProperties properties;

    @Override
    public Media uploadFile(MultipartFile file, Long uploadedBy) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        MediaStorageService.ValidationResult validation = storageService.validate(file);
        if (!validation.valid()) {
            throw new IllegalArgumentException(String.join(", ", validation.errors()));
        }

        // I/O Operation (Outside Transaction)
        MediaStorageService.StoredFileResult stored = storageService.store(file, "media");

        try {
            // Database Operations (Transactional)
            Media savedMedia = saveMediaAndCreateContainer(file, uploadedBy, stored);

            if (processingService.isProcessingSupported(stored.mimeType())) {
                processingService.generateFormats(savedMedia.getId());
            }

            return savedMedia;
        } catch (Exception e) {
            log.error("Failed to save media metadata, rolling back file storage: {}", stored.filePath());
            storageService.delete(stored.filePath());
            throw e;
        }
    }

    @Transactional
    protected Media saveMediaAndCreateContainer(MultipartFile file, Long uploadedBy,
            MediaStorageService.StoredFileResult stored) {
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

        if (processingService.isProcessingSupported(stored.mimeType())) {
            media.setStatus(MediaStatus.PROCESSING);
            byte[] content = storageService.retrieve(stored.filePath());
            ImageDimensions dimensions = processingService.extractDimensions(content);
            if (dimensions != null) {
                media.setWidth(dimensions.width());
                media.setHeight(dimensions.height());
            }
        } else {
            media.setStatus(MediaStatus.ACTIVE);
        }

        Media savedMedia = mediaRepository.save(media);
        log.info("File uploaded: {} with UID: {}", stored.fileName(), savedMedia.getUid());

        containerService.createForMedia(savedMedia.getId());

        return savedMedia;
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
    public void delete(Long id) {
        log.debug("Deleting media with ID: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        storageService.delete(media.getFilePath());
        mediaRepository.deleteById(id);
        log.info("Media deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> findAll() {
        return mediaRepository.findAll();
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
    public List<Media> findByFolderId(Long folderId) {
        return mediaRepository.findByFolderId(folderId);
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
}
