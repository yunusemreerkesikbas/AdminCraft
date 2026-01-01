package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.MediaI18nRepository;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MediaI18nService.
 * Handles localized metadata for media files.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaI18nServiceImpl implements MediaI18nService {

    private final MediaI18nRepository mediaI18nRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional
    public MediaI18n upsert(Long mediaId, Language language, String altText, String title, String description) {
        log.debug("Upserting MediaI18n for mediaId: {}, language: {}", mediaId, language);

        // Validate media exists
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with id: " + mediaId));

        // Find existing or create new
        MediaI18n mediaI18n = mediaI18nRepository.findByMediaIdAndLanguage(mediaId, language)
                .orElse(null);

        if (mediaI18n == null) {
            mediaI18n = new MediaI18n();
            mediaI18n.setMedia(media);
            mediaI18n.setLanguage(language);
            log.debug("Creating new MediaI18n for mediaId: {}, language: {}", mediaId, language);
        } else {
            log.debug("Updating existing MediaI18n id: {} for mediaId: {}, language: {}",
                    mediaI18n.getId(), mediaId, language);
        }

        // Update fields
        mediaI18n.setAltText(altText);
        mediaI18n.setTitle(title);
        mediaI18n.setDescription(description);

        MediaI18n saved = mediaI18nRepository.save(mediaI18n);
        log.info("Upserted MediaI18n id: {} for mediaId: {}, language: {}", saved.getId(), mediaId, language);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaI18n> get(Long mediaId, Language language) {
        log.debug("Getting MediaI18n for mediaId: {}, language: {}", mediaId, language);
        return mediaI18nRepository.findByMediaIdAndLanguage(mediaId, language);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaI18n> getByMediaId(Long mediaId) {
        log.debug("Getting all MediaI18n for mediaId: {}", mediaId);
        return mediaI18nRepository.findByMediaId(mediaId);
    }

    @Override
    @Transactional
    public void delete(Long mediaId, Language language) {
        log.debug("Deleting MediaI18n for mediaId: {}, language: {}", mediaId, language);
        mediaI18nRepository.deleteByMediaIdAndLanguage(mediaId, language);
        log.info("Deleted MediaI18n for mediaId: {}, language: {}", mediaId, language);
    }

    @Override
    @Transactional
    public void deleteAllByMediaId(Long mediaId) {
        log.debug("Deleting all MediaI18n for mediaId: {}", mediaId);
        mediaI18nRepository.deleteByMediaId(mediaId);
        log.info("Deleted all MediaI18n for mediaId: {}", mediaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long mediaId, Language language) {
        return mediaI18nRepository.existsByMediaIdAndLanguage(mediaId, language);
    }
}
