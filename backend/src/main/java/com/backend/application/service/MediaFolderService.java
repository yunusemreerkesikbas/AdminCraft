package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaFolder;

/**
 * Service interface for MediaFolder operations.
 * Handles hierarchical folder organization for media files.
 */
public interface MediaFolderService {

    // CRUD operations

    /**
     * Create a new folder.
     *
     * @param code      unique folder code (used in path)
     * @param name      display name
     * @param parentId  parent folder ID (null for root folder)
     * @param createdBy user ID who creates the folder
     * @return created MediaFolder
     * @throws IllegalArgumentException if code already exists or parent not found
     */
    MediaFolder create(String code, String name, Long parentId, Long createdBy);

    /**
     * Update folder name.
     *
     * @param id        folder ID
     * @param name      new display name
     * @param updatedBy user ID who updates the folder
     * @return updated MediaFolder
     * @throws IllegalArgumentException if folder not found
     */
    MediaFolder update(Long id, String name, Long updatedBy);

    /**
     * Delete a folder. Only empty folders can be deleted.
     *
     * @param id folder ID
     * @throws IllegalArgumentException if folder not found
     * @throws IllegalStateException    if folder is not empty
     */
    void delete(Long id);

    /**
     * Move a folder to a new parent.
     *
     * @param folderId    folder ID to move
     * @param newParentId new parent folder ID (null to move to root)
     * @return moved MediaFolder with updated path
     * @throws IllegalArgumentException if folder or parent not found
     * @throws IllegalStateException    if circular reference detected
     */
    MediaFolder move(Long folderId, Long newParentId);

    // Read operations

    /**
     * Find folder by ID.
     *
     * @param id folder ID
     * @return Optional containing MediaFolder if found
     */
    Optional<MediaFolder> findById(Long id);

    /**
     * Find folder by UID.
     *
     * @param uid folder UID
     * @return Optional containing MediaFolder if found
     */
    Optional<MediaFolder> findByUid(String uid);

    /**
     * Find folder by code.
     *
     * @param code folder code
     * @return Optional containing MediaFolder if found
     */
    Optional<MediaFolder> findByCode(String code);

    /**
     * Find folder by path.
     *
     * @param path folder path (e.g., "/images/products/")
     * @return Optional containing MediaFolder if found
     */
    Optional<MediaFolder> findByPath(String path);

    /**
     * Get all folders.
     *
     * @return list of all folders
     */
    List<MediaFolder> findAll();

    /**
     * Get root folders (folders without parent).
     *
     * @return list of root folders
     */
    List<MediaFolder> findRootFolders();

    /**
     * Get child folders of a parent.
     *
     * @param parentId parent folder ID
     * @return list of child folders
     */
    List<MediaFolder> findChildren(Long parentId);

    // Utility operations

    /**
     * Check if folder is empty (no media files inside).
     *
     * @param folderId folder ID
     * @return true if folder has no media files
     */
    boolean isEmpty(Long folderId);

    /**
     * Generate path for a new folder.
     *
     * @param parentId parent folder ID (null for root)
     * @param code     folder code
     * @return generated path (e.g., "/images/products/shoes/")
     */
    String generatePath(Long parentId, String code);
}
