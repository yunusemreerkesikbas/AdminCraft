package com.backend.application.service;

import java.util.Optional;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaContainer;

public interface MediaContainerService {

    MediaContainer createForMedia(Long mediaId);

    void addVariant(Long masterId, Long formatId, Long variantMediaId);

    Optional<MediaContainer> getByMasterMediaId(Long masterMediaId);

    /**
     * Alias for getByMasterMediaId for consistent naming.
     */
    default Optional<MediaContainer> findByMasterMediaId(Long masterMediaId) {
        return getByMasterMediaId(masterMediaId);
    }

    Optional<Media> getVariant(Long masterId, String formatCode);

    Optional<MediaContainer> findById(Long id);

    void delete(Long id);
}
