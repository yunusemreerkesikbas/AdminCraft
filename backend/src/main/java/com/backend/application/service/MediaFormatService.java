package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;

/**
 * Service interface for MediaFormat operations.
 * Handles image format definitions for variant generation.
 * System formats are protected and cannot be modified.
 */
public interface MediaFormatService {

    // CRUD operations

    /**
     * Create a new custom format.
     *
     * @param code      unique format code
     * @param name      display name
     * @param width     target width in pixels (1-10000)
     * @param height    target height in pixels (1-10000)
     * @param quality   JPEG quality (1-100, default 80)
     * @param cropMode  crop mode (FIT, FILL, COVER)
     * @param createdBy user ID who creates the format
     * @return created MediaFormat
     * @throws IllegalArgumentException if code already exists
     */
    MediaFormat create(String code, String name, Integer width, Integer height,
            Integer quality, CropMode cropMode, Long createdBy);

    /**
     * Update a custom format.
     * System formats cannot be updated.
     *
     * @param id        format ID
     * @param name      new display name
     * @param width     new width in pixels
     * @param height    new height in pixels
     * @param quality   new quality
     * @param cropMode  new crop mode
     * @param updatedBy user ID who updates the format
     * @return updated MediaFormat
     * @throws IllegalArgumentException if format not found
     * @throws IllegalStateException    if format is a system format
     */
    MediaFormat update(Long id, String name, Integer width, Integer height,
            Integer quality, CropMode cropMode, Long updatedBy);

    /**
     * Delete a custom format.
     * System formats cannot be deleted.
     *
     * @param id format ID
     * @throws IllegalArgumentException if format not found
     * @throws IllegalStateException    if format is a system format
     */
    void delete(Long id);

    // Read operations

    /**
     * Find format by ID.
     *
     * @param id format ID
     * @return Optional containing MediaFormat if found
     */
    Optional<MediaFormat> findById(Long id);

    /**
     * Find format by UID.
     *
     * @param uid format UID
     * @return Optional containing MediaFormat if found
     */
    Optional<MediaFormat> findByUid(String uid);

    /**
     * Find format by code.
     *
     * @param code format code
     * @return Optional containing MediaFormat if found
     */
    Optional<MediaFormat> findByCode(String code);

    /**
     * Get all formats (both system and custom).
     *
     * @return list of all formats
     */
    List<MediaFormat> findAll();

    /**
     * Get system formats only.
     *
     * @return list of system formats
     */
    List<MediaFormat> findSystemFormats();

    /**
     * Get custom formats only.
     *
     * @return list of custom formats
     */
    List<MediaFormat> findCustomFormats();

    // Utility operations

    /**
     * Check if format is a system format.
     *
     * @param id format ID
     * @return true if system format
     */
    boolean isSystemFormat(Long id);
}
