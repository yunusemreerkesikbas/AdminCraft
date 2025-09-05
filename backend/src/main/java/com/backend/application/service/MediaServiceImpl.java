package com.backend.application.service;

import com.backend.domain.entity.MediaFile;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final com.backend.application.service.impl.StorageService storageService;

    @Value("${admincraft.media.upload-path:uploads}")
    private String uploadPath;

    @Value("${admincraft.media.max-file-size:10485760}") // 10MB
    private Long maxFileSize;

    @Value("${admincraft.media.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,mp4,mp3}")
    private String allowedExtensions;

    @Override
    public MediaFile uploadFile(MultipartFile file, Long tenantId, Long uploadedBy) {
        log.debug("Uploading file: {} for tenant: {}", file.getOriginalFilename(), tenantId);

        // Validate file
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
            // Sprint 7: SHA-256 de-duplication check
            String contentHash = calculateSHA256(file.getBytes());
            
            // Check if file already exists for this tenant
            Optional<MediaFile> existingFile = mediaRepository.findByTenantIdAndContentHash(tenantId, contentHash);
            if (existingFile.isPresent()) {
                log.debug("Duplicate file detected for tenant: {}, returning existing file: {}", 
                    tenantId, existingFile.get().getFileName());
                return existingFile.get();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(extension);

            // Sprint 7: Use tenant namespaced storage path
            Path filePath = storageService.saveToTenantFolder(file.getBytes(), tenantId, uniqueFilename);

            // Create MediaFile entity with Sprint 7 enhancements
            MediaFile mediaFile = new MediaFile();
            mediaFile.setOriginalName(originalFilename);
            mediaFile.setFileName(uniqueFilename);
            mediaFile.setFilePath(filePath.toString());
            mediaFile.setMimeType(file.getContentType());
            mediaFile.setFileSize(file.getSize());
            mediaFile.setFileExtension(extension);
            mediaFile.setTenantId(tenantId);
            mediaFile.setUploadedBy(uploadedBy);
            mediaFile.setFolder("uploads");
            mediaFile.setCategory("general");
            mediaFile.setStorageProvider("local");
            
            // Sprint 7: Content hash for de-duplication
            mediaFile.setContentHash(contentHash);
            
            // Sprint 7: Staged upload system - initially STAGED
            // Will be set to ACTIVE when first attached or explicitly finalized
            // Note: Using MediaStatus enum (to be created) if not exists yet

            // Extract image dimensions and create thumbnail/variants if it's an image
            if (mediaFile.isImage()) {
                extractImageDimensions(mediaFile, filePath);
                try {
                    storageService.generateThumbnail(mediaFile, 400, 400);
                    storageService.generateVariants(mediaFile);
                } catch (IOException ioe) {
                    log.warn("Thumbnail generation failed for {}: {}", uniqueFilename, ioe.getMessage());
                }
            }

            MediaFile savedFile = mediaRepository.save(mediaFile);
            log.info("File uploaded successfully: {} with ID: {}", uniqueFilename, savedFile.getId());

            return savedFile;

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFile> getMediaFileById(Long id) {
        return mediaRepository.findById(id);
    }

    @Override
    public MediaFile updateMediaFile(MediaFile mediaFile) {
        log.debug("Updating media file with ID: {}", mediaFile.getId());

        if (!mediaRepository.existsById(mediaFile.getId())) {
            throw new IllegalArgumentException("Media file not found with ID: " + mediaFile.getId());
        }

        MediaFile updatedFile = mediaRepository.save(mediaFile);
        log.info("Media file updated successfully with ID: {}", updatedFile.getId());

        return updatedFile;
    }

    @Override
    public void deleteMediaFile(Long id) {
        log.debug("Deleting media file with ID: {}", id);

        MediaFile mediaFile = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found with ID: " + id));

        if (!mediaFile.canBeDeleted()) {
            throw new IllegalStateException("Media file is still in use and cannot be deleted");
        }

        try {
            // Delete physical file
            Path filePath = Paths.get(mediaFile.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete thumbnail if exists
            if (mediaFile.getHasThumbnails() && mediaFile.getThumbnailPath() != null) {
                Path thumbnailPath = Paths.get(mediaFile.getThumbnailPath());
                Files.deleteIfExists(thumbnailPath);
            }

            // Delete from database
            mediaRepository.deleteById(id);
            log.info("Media file deleted successfully: {}", id);

        } catch (IOException e) {
            log.error("Error deleting physical file: {}", e.getMessage());
            // Still delete from database even if physical file deletion fails
            mediaRepository.deleteById(id);
            throw new RuntimeException("Failed to delete physical file", e);
        }
    }

    // REMOVED: getAllMediaFiles() - CRITICAL SECURITY FIX
    // This method was removed to prevent cross-tenant data access
    // Use getMediaFilesByTenantId(Long tenantId) instead

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFile> getMediaFileByFileName(String fileName) {
        return mediaRepository.findByFileName(fileName);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFileContent(String fileName) {
        MediaFile mediaFile = mediaRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        try {
            Path filePath = Paths.get(mediaFile.getFilePath());

            // Update last accessed time
            mediaFile.setLastAccessedAt(LocalDateTime.now());
            mediaRepository.save(mediaFile);

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Error reading file content: {}", e.getMessage());
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getThumbnailContent(String fileName) {
        MediaFile mediaFile = mediaRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        if (!mediaFile.getHasThumbnails() || mediaFile.getThumbnailPath() == null) {
            // Return original file if no thumbnail exists
            return getFileContent(fileName);
        }

        try {
            Path thumbnailPath = Paths.get(mediaFile.getThumbnailPath());
            return Files.readAllBytes(thumbnailPath);

        } catch (IOException e) {
            log.error("Error reading thumbnail content: {}", e.getMessage());
            // Fallback to original file
            return getFileContent(fileName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFile> getMediaFilesByTenantId(Long tenantId) {
        return mediaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalStorageUsed(Long tenantId) {
        Long totalSize = mediaRepository.sumFileSizeByTenantId(tenantId);
        return totalSize != null ? totalSize : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFile> getImageFiles(Long tenantId) {
        return mediaRepository.findByTenantIdAndMimeTypeStartingWith(tenantId, "image/");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFile> getVideoFiles(Long tenantId) {
        return mediaRepository.findByTenantIdAndMimeTypeStartingWith(tenantId, "video/");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFile> getDocumentFiles(Long tenantId) {
        return mediaRepository.findByTenantIdAndMimeTypeStartingWith(tenantId, "application/");
    }

    @Override
    public MediaFile updateAltText(Long id, Language language, String altText) {
        MediaFile mediaFile = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found"));

        mediaFile.setAltText(language, altText);

        return mediaRepository.save(mediaFile);
    }

    @Override
    public MediaFile updateDescription(Long id, Language language, String description) {
        MediaFile mediaFile = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found"));

        mediaFile.setDescription(language, description);

        return mediaRepository.save(mediaFile);
    }

    @Override
    public void incrementUsage(Long id) {
        MediaFile mediaFile = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found"));

        mediaFile.incrementUsage();
        mediaRepository.save(mediaFile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFile> searchByName(Long tenantId, String searchTerm) {
        return mediaRepository.findByTenantIdAndOriginalNameContainingIgnoreCase(tenantId, searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null)
            return false;

        String extension = getFileExtension(filename).toLowerCase();
        return List.of(allowedExtensions.split(",")).contains(extension);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFileSizeAllowed(MultipartFile file, Long maxSize) {
        return file.getSize() <= maxSize;
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

    private void extractImageDimensions(MediaFile mediaFile, Path filePath) {
        // TODO: Implement image dimension extraction using ImageIO or similar
        // For now, setting default values
        mediaFile.setWidth(0);
        mediaFile.setHeight(0);
    }

    // Implementing placeholder methods to satisfy interface (simplified for
    // brevity)
    @Override
    public String getFileUrl(Long id) {
        return "/api/media/files/" + id;
    }

    @Override
    public String getThumbnailUrl(Long id) {
        return "/api/media/thumbnails/" + id;
    }

    @Override
    public List<MediaFile> getRecentMediaFiles(Long tenantId, int limit) {
        return List.of();
    }

    @Override
    public long countMediaFilesByTenantId(Long tenantId) {
        return mediaRepository.countByTenantId(tenantId);
    }

    @Override
    public List<MediaFile> getAudioFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesByMimeType(Long tenantId, String mimeType) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesByExtension(Long tenantId, String extension) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesByFolder(Long tenantId, String folder) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesByCategory(Long tenantId, String category) {
        return List.of();
    }

    @Override
    public List<String> getAllFolders(Long tenantId) {
        return List.of();
    }

    @Override
    public List<String> getAllCategories(Long tenantId) {
        return List.of();
    }

    @Override
    public MediaFile moveToFolder(Long id, String folder) {
        return null;
    }

    @Override
    public MediaFile changeCategory(Long id, String category) {
        return null;
    }

    @Override
    public MediaFile updateTitle(Long id, Language language, String title) {
        return null;
    }

    @Override
    public String getLocalizedAltText(Long id, Language language) {
        return "";
    }

    @Override
    public String getLocalizedDescription(Long id, Language language) {
        return "";
    }

    @Override
    public String getLocalizedTitle(Long id, Language language) {
        return "";
    }

    @Override
    public List<MediaFile> searchByAltText(Long tenantId, String searchTerm, Language language) {
        return List.of();
    }

    @Override
    public List<MediaFile> searchByDescription(Long tenantId, String searchTerm, Language language) {
        return List.of();
    }

    @Override
    public List<MediaFile> searchByTags(Long tenantId, String tag) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesBySize(Long tenantId, Long minSize, Long maxSize) {
        return List.of();
    }

    @Override
    public List<MediaFile> getLargeFiles(Long tenantId, Long minSize) {
        return List.of();
    }

    @Override
    public List<MediaFile> getImagesByDimensions(Long tenantId, Integer minWidth, Integer minHeight) {
        return List.of();
    }

    @Override
    public List<MediaFile> getLargestFiles(Long tenantId, int limit) {
        return List.of();
    }

    @Override
    public MediaFile generateThumbnails(Long id) {
        return null;
    }

    @Override
    public MediaFile optimizeImage(Long id) {
        return null;
    }

    @Override
    public List<MediaFile> getUnoptimizedImages(Long tenantId) {
        return List.of();
    }

    @Override
    public MediaFile resizeImage(Long id, Integer width, Integer height) {
        return null;
    }

    @Override
    public List<MediaFile> getMostUsedFiles(Long tenantId, int limit) {
        return List.of();
    }

    @Override
    public List<MediaFile> getUnusedFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public List<MediaFile> getFilesByUsageCount(Long tenantId, int minUsageCount) {
        return List.of();
    }

    @Override
    public List<MediaFile> getPublicFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public List<MediaFile> getPrivateFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public MediaFile makeFilePublic(Long id) {
        return null;
    }

    @Override
    public MediaFile makeFilePrivate(Long id) {
        return null;
    }

    @Override
    public List<MediaFile> getFilesByStorageProvider(Long tenantId, String storageProvider) {
        return List.of();
    }

    @Override
    public MediaFile migrateToStorageProvider(Long id, String newProvider) {
        return null;
    }

    @Override
    public void syncWithExternalStorage(Long tenantId) {
    }

    @Override
    public List<MediaFile> getFilesUploadedBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    @Override
    public List<MediaFile> getRecentlyAccessedFiles(Long tenantId, LocalDateTime since) {
        return List.of();
    }

    @Override
    public List<MediaFile> getOldFiles(Long tenantId, LocalDateTime before) {
        return List.of();
    }

    @Override
    public void bulkDelete(List<Long> fileIds) {
    }

    @Override
    public void bulkMoveToFolder(List<Long> fileIds, String folder) {
    }

    @Override
    public void bulkChangeCategory(List<Long> fileIds, String category) {
    }

    @Override
    public void bulkMakePublic(List<Long> fileIds) {
    }

    @Override
    public void bulkMakePrivate(List<Long> fileIds) {
    }

    @Override
    public void bulkOptimize(List<Long> fileIds) {
    }

    @Override
    public void deleteFilesByTenantId(Long tenantId) {
    }

    @Override
    public List<String> getAllowedExtensions() {
        return List.of(allowedExtensions.split(","));
    }

    @Override
    public List<String> getAllowedMimeTypes() {
        return List.of();
    }

    @Override
    public List<MediaFile> findDuplicateFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public Optional<MediaFile> findDuplicateByHash(String fileHash, Long tenantId) {
        return Optional.empty();
    }

    @Override
    public MediaFile checkForDuplicate(MultipartFile file, Long tenantId) {
        return null;
    }

    @Override
    public long getTotalFilesCount(Long tenantId) {
        // Sprint 7: Tenant-scoped file count
        return mediaRepository.countByTenantId(tenantId);
    }

    @Override
    public long getImageFilesCount(Long tenantId) {
        return 0;
    }

    @Override
    public long getVideoFilesCount(Long tenantId) {
        return 0;
    }

    @Override
    public long getDocumentFilesCount(Long tenantId) {
        return 0;
    }

    @Override
    public long getFilesCountByExtension(Long tenantId, String extension) {
        return 0;
    }

    @Override
    public List<MediaFile> getOrphanedFiles(Long tenantId) {
        return List.of();
    }

    @Override
    public void cleanupUnusedFiles(Long tenantId, int unusedDays) {
    }

    @Override
    public void cleanupThumbnails(Long tenantId) {
    }

    @Override
    public void validateFileIntegrity(Long tenantId) {
    }

    @Override
    public MediaFile extractMetadata(Long id) {
        return null;
    }

    @Override
    public MediaFile updateMetadata(Long id, String metadata) {
        return null;
    }

    @Override
    public String getFileMetadata(Long id) {
        return "";
    }

    @Override
    public List<MediaFile> exportMediaList(Long tenantId) {
        return List.of();
    }

    @Override
    public void backupFiles(Long tenantId, String backupPath) {
    }

    @Override
    public void restoreFiles(Long tenantId, String backupPath) {
    }
    
    // Sprint 7: SHA-256 De-duplication utility methods
    private String calculateSHA256(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileContent);
            StringBuilder hexString = new StringBuilder();
            
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
}