package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;
import com.backend.domain.repository.MediaFormatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MediaFormatService.
 * Handles image format definitions with system format protection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFormatServiceImpl implements MediaFormatService {

    private final MediaFormatRepository mediaFormatRepository;

    @Override
    @Transactional
    public MediaFormat create(String code, String name, Integer width, Integer height,
            Integer quality, CropMode cropMode, Long createdBy) {
        log.debug("Creating format with code: {}, name: {}, dimensions: {}x{}", code, name, width, height);

        // Validate code uniqueness
        if (mediaFormatRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Format code already exists: " + code);
        }

        // Validate dimensions
        validateDimensions(width, height);
        validateQuality(quality);

        MediaFormat format = new MediaFormat();
        format.setCode(code);
        format.setName(name);
        format.setWidth(width);
        format.setHeight(height);
        format.setQuality(quality != null ? quality : 80);
        format.setCropMode(cropMode != null ? cropMode : CropMode.FIT);
        format.setIsSystem(false); // Custom formats are not system formats
        format.setCreatedBy(createdBy);
        format.setUpdatedBy(createdBy);

        MediaFormat saved = mediaFormatRepository.save(format);
        log.info("Created format id: {}, code: {}, dimensions: {}", saved.getId(), saved.getCode(),
                saved.getDimensions());

        return saved;
    }

    @Override
    @Transactional
    public MediaFormat update(Long id, String name, Integer width, Integer height,
            Integer quality, CropMode cropMode, Long updatedBy) {
        log.debug("Updating format id: {} with name: {}, dimensions: {}x{}", id, name, width, height);

        MediaFormat format = mediaFormatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Format not found with id: " + id));

        // System formats cannot be modified
        if (format.isSystemFormat()) {
            throw new IllegalStateException("Cannot modify system format: " + format.getCode());
        }

        // Validate dimensions
        validateDimensions(width, height);
        validateQuality(quality);

        // Only update fields when non-null values are provided (preserve existing
        // values otherwise)
        format.setName(name != null ? name : format.getName());
        format.setWidth(width);
        format.setHeight(height);
        format.setQuality(quality != null ? quality : format.getQuality());
        format.setCropMode(cropMode != null ? cropMode : format.getCropMode());
        format.setUpdatedBy(updatedBy);

        MediaFormat saved = mediaFormatRepository.save(format);
        log.info("Updated format id: {}, code: {}, dimensions: {}", saved.getId(), saved.getCode(),
                saved.getDimensions());

        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting format id: {}", id);

        MediaFormat format = mediaFormatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Format not found with id: " + id));

        // System formats cannot be deleted
        if (format.isSystemFormat()) {
            throw new IllegalStateException("Cannot delete system format: " + format.getCode());
        }

        mediaFormatRepository.delete(format);
        log.info("Deleted format id: {}, code: {}", id, format.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFormat> findById(Long id) {
        return mediaFormatRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFormat> findByUid(String uid) {
        return mediaFormatRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaFormat> findByCode(String code) {
        return mediaFormatRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFormat> findAll() {
        return mediaFormatRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFormat> findSystemFormats() {
        return mediaFormatRepository.findByIsSystemTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFormat> findCustomFormats() {
        return mediaFormatRepository.findByIsSystemFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSystemFormat(Long id) {
        return mediaFormatRepository.findById(id)
                .map(MediaFormat::isSystemFormat)
                .orElse(false);
    }

    /**
     * Validate dimension values.
     */
    private void validateDimensions(Integer width, Integer height) {
        if (width == null || width < 1 || width > 10000) {
            throw new IllegalArgumentException("Width must be between 1 and 10000 pixels");
        }
        if (height == null || height < 1 || height > 10000) {
            throw new IllegalArgumentException("Height must be between 1 and 10000 pixels");
        }
    }

    /**
     * Validate quality value.
     */
    private void validateQuality(Integer quality) {
        if (quality != null && (quality < 1 || quality > 100)) {
            throw new IllegalArgumentException("Quality must be between 1 and 100");
        }
    }
}
