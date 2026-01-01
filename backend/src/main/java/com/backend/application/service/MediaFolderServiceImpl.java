package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.MediaFolder;
import com.backend.domain.repository.MediaFolderRepository;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MediaFolderService.
 * Handles hierarchical folder organization with path-based navigation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFolderServiceImpl implements MediaFolderService {

    private final MediaFolderRepository mediaFolderRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional
    public MediaFolder create(String code, String name, Long parentId, Long createdBy) {
        log.debug("Creating folder with code: {}, name: {}, parentId: {}", code, name, parentId);

        // Validate code uniqueness
        if (mediaFolderRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Folder code already exists: " + code);
        }

        MediaFolder folder = new MediaFolder();
        folder.setCode(code);
        folder.setName(name);
        folder.setCreatedBy(createdBy);
        folder.setUpdatedBy(createdBy);

        // Set parent and calculate path
        if (parentId != null) {
            MediaFolder parent = mediaFolderRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent folder not found with id: " + parentId));
            folder.setParent(parent);
        }

        // Update path using entity method
        folder.updatePath();

        // Validate path uniqueness
        if (mediaFolderRepository.existsByPath(folder.getPath())) {
            throw new IllegalArgumentException("Folder path already exists: " + folder.getPath());
        }

        MediaFolder saved = mediaFolderRepository.save(folder);
        log.info("Created folder id: {}, code: {}, path: {}", saved.getId(), saved.getCode(), saved.getPath());

        return saved;
    }

    @Override
    @Transactional
    public MediaFolder update(Long id, String name, Long updatedBy) {
        log.debug("Updating folder id: {} with name: {}", id, name);

        MediaFolder folder = mediaFolderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id: " + id));

        folder.setName(name);
        folder.setUpdatedBy(updatedBy);

        MediaFolder saved = mediaFolderRepository.save(folder);
        log.info("Updated folder id: {}, name: {}", saved.getId(), saved.getName());

        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting folder id: {}", id);

        MediaFolder folder = mediaFolderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id: " + id));

        // Check if folder is empty (no media files)
        if (!isEmpty(id)) {
            throw new IllegalStateException("Cannot delete folder: folder contains media files. Folder id: " + id);
        }

        // Check if folder has subfolders
        if (folder.hasChildren()) {
            throw new IllegalStateException("Cannot delete folder: folder has subfolders. Folder id: " + id);
        }

        mediaFolderRepository.delete(folder);
        log.info("Deleted folder id: {}, code: {}", id, folder.getCode());
    }

    @Override
    @Transactional
    public MediaFolder move(Long folderId, Long newParentId) {
        log.debug("Moving folder id: {} to new parentId: {}", folderId, newParentId);

        MediaFolder folder = mediaFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id: " + folderId));

        // Prevent moving to itself
        if (newParentId != null && newParentId.equals(folderId)) {
            throw new IllegalStateException("Cannot move folder to itself. Folder id: " + folderId);
        }

        MediaFolder newParent = null;
        if (newParentId != null) {
            newParent = mediaFolderRepository.findById(newParentId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("New parent folder not found with id: " + newParentId));

            // Prevent circular reference
            if (isDescendantOf(newParent, folder)) {
                throw new IllegalStateException(
                        "Cannot move folder: circular reference detected. Folder id: " + folderId
                                + ", newParentId: " + newParentId);
            }
        }

        // Store old path prefix and depth for descendant updates
        String oldPathPrefix = folder.getPath();
        int oldDepth = folder.getDepth();

        // Update parent and path
        folder.setParent(newParent);
        folder.updatePath();

        // Validate new path uniqueness
        if (mediaFolderRepository.existsByPath(folder.getPath())) {
            throw new IllegalArgumentException("Target path already exists: " + folder.getPath());
        }

        // Update descendant paths and depths
        updateDescendantPaths(folder, oldPathPrefix, oldDepth);

        MediaFolder saved = mediaFolderRepository.save(folder);
        log.info("Moved folder id: {} from {} to {}", folderId, oldPathPrefix, saved.getPath());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFolder> findById(Long id) {
        return mediaFolderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFolder> findByUid(String uid) {
        return mediaFolderRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFolder> findByCode(String code) {
        return mediaFolderRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFolder> findByPath(String path) {
        return mediaFolderRepository.findByPath(path);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFolder> findAll() {
        return mediaFolderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFolder> findRootFolders() {
        return mediaFolderRepository.findByParentIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFolder> findChildren(Long parentId) {
        return mediaFolderRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmpty(Long folderId) {
        return mediaRepository.countByFolderId(folderId) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public String generatePath(Long parentId, String code) {
        if (parentId == null) {
            return "/" + code + "/";
        }

        MediaFolder parent = mediaFolderRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent folder not found with id: " + parentId));

        return parent.getPath() + code + "/";
    }

    /**
     * Check if a folder is descendant of another folder.
     */
    private boolean isDescendantOf(MediaFolder possibleDescendant, MediaFolder ancestor) {
        MediaFolder current = possibleDescendant;
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Update paths and depths of all descendant folders after a move operation.
     * 
     * @param folder        the moved folder with updated path/depth
     * @param oldPathPrefix the folder's path before the move
     * @param oldDepth      the folder's depth before the move
     */
    private void updateDescendantPaths(MediaFolder folder, String oldPathPrefix, int oldDepth) {
        // Find all descendants by path prefix
        List<MediaFolder> descendants = mediaFolderRepository.findByPathStartingWith(oldPathPrefix);

        // Calculate depth change: positive = moved deeper, negative = moved shallower
        int depthDelta = folder.getDepth() - oldDepth;

        for (MediaFolder descendant : descendants) {
            if (!descendant.getId().equals(folder.getId())) {
                // Replace old path prefix with new path
                String newPath = folder.getPath() + descendant.getPath().substring(oldPathPrefix.length());
                descendant.setPath(newPath);

                // Apply depth delta to descendant
                int newDepth = descendant.getDepth() + depthDelta;
                descendant.setDepth(newDepth);

                mediaFolderRepository.save(descendant);
                log.debug("Updated descendant folder id: {} to path: {}, depth: {}",
                        descendant.getId(), newPath, newDepth);
            }
        }
    }
}
