package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepository {

    private final MediaJpaRepository jpaRepository;

    @Override
    public Media save(Media media) {
        return jpaRepository.save(media);
    }

    @Override
    public Optional<Media> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Media> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<Media> findByUuid(String uuid) {
        return jpaRepository.findByUuid(uuid);
    }

    @Override
    public List<Media> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(Media media) {
        jpaRepository.delete(media);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public Optional<Media> findByFileName(String fileName) {
        return jpaRepository.findByFileName(fileName);
    }

    @Override
    public boolean existsByFileName(String fileName) {
        return jpaRepository.existsByFileName(fileName);
    }

    @Override
    public List<Media> findByFolderId(Long folderId) {
        return jpaRepository.findByFolderId(folderId);
    }

    @Override
    public List<Media> findByFolderIsNull() {
        return jpaRepository.findByFolderIsNull();
    }

    @Override
    public long countByFolderId(Long folderId) {
        return jpaRepository.countByFolderId(folderId);
    }

    @Override
    public List<Media> findByMimeTypeStartingWith(String mimeTypePrefix) {
        return jpaRepository.findByMimeTypeStartingWith(mimeTypePrefix);
    }

    @Override
    public List<Media> findByFileExtension(String extension) {
        return jpaRepository.findByFileExtension(extension);
    }

    @Override
    public List<Media> findByFileExtensionIn(List<String> extensions) {
        return jpaRepository.findByFileExtensionIn(extensions);
    }

    @Override
    public List<Media> findByWidthIsNotNull() {
        return jpaRepository.findByWidthIsNotNull();
    }

    @Override
    public List<Media> findByWidthBetween(Integer minWidth, Integer maxWidth) {
        return jpaRepository.findByWidthBetween(minWidth, maxWidth);
    }

    @Override
    public List<Media> findByHeightBetween(Integer minHeight, Integer maxHeight) {
        return jpaRepository.findByHeightBetween(minHeight, maxHeight);
    }

    @Override
    public List<Media> findByFileSizeBetween(Long minSize, Long maxSize) {
        return jpaRepository.findByFileSizeBetween(minSize, maxSize);
    }

    @Override
    public List<Media> findByFileSizeGreaterThan(Long size) {
        return jpaRepository.findByFileSizeGreaterThan(size);
    }

    @Override
    public List<Media> findAllOrderByFileSizeDesc() {
        return jpaRepository.findAllByOrderByFileSizeDesc();
    }

    @Override
    public List<Media> findByStatus(MediaStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public long countByStatus(MediaStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Media> findByIsPublicTrue() {
        return jpaRepository.findByIsPublicTrue();
    }

    @Override
    public List<Media> findByIsPublicFalse() {
        return jpaRepository.findByIsPublicFalse();
    }

    @Override
    public List<Media> findByStorageProvider(StorageProvider storageProvider) {
        return jpaRepository.findByStorageProvider(storageProvider);
    }

    @Override
    public List<Media> findByExternalIdIsNotNull() {
        return jpaRepository.findByExternalIdIsNotNull();
    }

    @Override
    public List<Media> findByUploadedBy(Long userId) {
        return jpaRepository.findByUploadedBy(userId);
    }

    @Override
    public long countByUploadedBy(Long userId) {
        return jpaRepository.countByUploadedBy(userId);
    }

    @Override
    public List<Media> findByOriginalNameContainingIgnoreCase(String originalName) {
        return jpaRepository.findByOriginalNameContainingIgnoreCase(originalName);
    }

    @Override
    public List<Media> findByUsageCountGreaterThan(Integer count) {
        return jpaRepository.findByUsageCountGreaterThan(count);
    }

    @Override
    public List<Media> findByUsageCount(Integer count) {
        return jpaRepository.findByUsageCount(count);
    }

    @Override
    public List<Media> findAllOrderByUsageCountDesc() {
        return jpaRepository.findAllByOrderByUsageCountDesc();
    }

    @Override
    public List<Media> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Media> findByLastAccessedAtBefore(LocalDateTime dateTime) {
        return jpaRepository.findByLastAccessedAtBefore(dateTime);
    }

    @Override
    public List<Media> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Media> findByIdIn(List<Long> ids) {
        return jpaRepository.findByIdIn(ids);
    }

    @Override
    public List<Media> findByUidIn(List<String> uids) {
        return jpaRepository.findByUidIn(uids);
    }

    @Override
    public List<Media> findByFileNameIn(List<String> fileNames) {
        return jpaRepository.findByFileNameIn(fileNames);
    }

    @Override
    public void deleteByUploadedBy(Long userId) {
        jpaRepository.deleteByUploadedBy(userId);
    }

    @Override
    public List<Media> findUnusedMedia() {
        return jpaRepository.findUnusedMedia();
    }

    @Override
    public List<Media> findLargestMedia(int limit) {
        return jpaRepository.findAllByOrderByFileSizeDesc().stream()
                .limit(limit)
                .toList();
    }

    @Override
    public long countByMimeTypeStartingWith(String mimeTypePrefix) {
        return jpaRepository.countByMimeTypeStartingWith(mimeTypePrefix);
    }

    @Override
    public long sumFileSize() {
        Long sum = jpaRepository.sumFileSize();
        return sum != null ? sum : 0L;
    }
}