package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;

/**
 * Service interface for MediaI18n operations.
 * Handles localized metadata for media files (alt text, title, description).
 */
public interface MediaI18nService {

    /**
     * Upsert (find-or-create) i18n metadata for a media file.
     *
     * @param mediaId     the media file ID
     * @param language    the language
     * @param altText     alt text for accessibility
     * @param title       title of the media
     * @param description description of the media
     * @return the upserted MediaI18n entity
     * @throws IllegalArgumentException if media not found
     */
    MediaI18n upsert(Long mediaId, Language language, String altText, String title, String description);

    /**
     * Get i18n metadata for a specific media and language.
     *
     * @param mediaId  the media file ID
     * @param language the language
     * @return Optional containing MediaI18n if found
     */
    Optional<MediaI18n> get(Long mediaId, Language language);

    /**
     * Get all i18n metadata for a media file (all languages).
     *
     * @param mediaId the media file ID
     * @return list of MediaI18n entities
     */
    List<MediaI18n> getByMediaId(Long mediaId);

    /**
     * Delete i18n metadata for a specific media and language.
     *
     * @param mediaId  the media file ID
     * @param language the language
     */
    void delete(Long mediaId, Language language);

    /**
     * Delete all i18n metadata for a media file.
     *
     * @param mediaId the media file ID
     */
    void deleteAllByMediaId(Long mediaId);

    /**
     * Check if i18n metadata exists for a media and language.
     *
     * @param mediaId  the media file ID
     * @param language the language
     * @return true if exists
     */
    boolean exists(Long mediaId, Language language);
}
