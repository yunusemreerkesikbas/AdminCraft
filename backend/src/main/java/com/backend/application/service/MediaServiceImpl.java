package com.backend.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Media service implementation.
 * Phase 1 - Core CRUD operations only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @Value("${admincraft.media.upload-path:uploads}")
    private String uploadPath;

    @Value("${admincraft.media.max-file-size:10485760}")
    private Long maxFileSize;

    @Value("${admincraft.media.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,mp4,mp3,webp}")
    private String allowedExtensions;

    @Override
    public Media uploadFile(MultipartFile file, Long uploadedBy) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        if (!isFileSizeAllowed(file, maxFileSize)) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(extension);

            Path uploadDir = Paths.get(uploadPath);
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media();
            media.setOriginalName(originalFilename);
            media.setFileName(uniqueFilename);
            media.setFilePath(filePath.toString());
            media.setMimeType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setFileExtension(extension);
            media.setUploadedBy(uploadedBy);
            media.setStorageProvider(StorageProvider.LOCAL);
            media.setStatus(MediaStatus.ACTIVE);
            media.setIsPublic(true);
            media.setUsageCount(0);

            Media savedMedia = mediaRepository.save(media);
            log.info("File uploaded successfully: {} with UID: {}", uniqueFilename, savedMedia.getUid());

            return savedMedia;

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
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
    public Media update(Media media) {
        log.debug("Updating media with UID: {}", media.getUid());

        if (!mediaRepository.existsById(media.getId())) {
            throw new IllegalArgumentException("Media not found with ID: " + media.getId());
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media updated successfully with UID: {}", updatedMedia.getUid());

        return updatedMedia;
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting media with ID: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        try {
            Path filePath = Paths.get(media.getFilePath());
            Files.deleteIfExists(filePath);

            mediaRepository.deleteById(id);
            log.info("Media deleted successfully: {}", id);

        } catch (IOException e) {
            log.error("Error deleting physical file for media ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete physical file", e);
        }
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

        try {
            Path filePath = Paths.get(media.getFilePath());
            media.recordAccess();
            mediaRepository.save(media);

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Error reading file content: {}", e.getMessage());
            throw new RuntimeException("Failed to read file content", e);
        }
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
        String filename = file.getOriginalFilename();
        if (filename == null)
            return false;

        String extension = getFileExtension(filename).toLowerCase();
        return List.of(allowedExtensions.split(",")).contains(extension);
    }

    @Override
    public boolean isFileSizeAllowed(MultipartFile file, Long maxSize) {
        return file.getSize() <= maxSize;
    }

    @Override
    public List<String> getAllowedExtensions() {
        return List.of(allowedExtensions.split(","));
    }

    private String getFileExtension(String filename) {
        if (filename == null)
            return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }
}