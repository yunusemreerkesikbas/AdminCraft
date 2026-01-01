package com.backend.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaContainer;
import com.backend.domain.entity.MediaContainerItem;
import com.backend.domain.entity.MediaFormat;
import com.backend.domain.repository.MediaContainerRepository;
import com.backend.domain.repository.MediaFormatRepository;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaContainerServiceImpl implements MediaContainerService {

    private final MediaContainerRepository containerRepository;
    private final MediaRepository mediaRepository;
    private final MediaFormatRepository formatRepository;

    @Override
    public MediaContainer createForMedia(Long mediaId) {
        if (containerRepository.existsByMasterMediaId(mediaId)) {
            return containerRepository.findByMasterMediaId(mediaId).orElseThrow();
        }

        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        MediaContainer container = new MediaContainer();
        container.setCode("container_" + UUID.randomUUID().toString().substring(0, 8));
        container.setMasterMedia(media);

        container = containerRepository.save(container);
        log.info("Created container {} for media {}", container.getCode(), mediaId);
        return container;
    }

    @Override
    public void addVariant(Long masterId, Long formatId, Long variantMediaId) {
        MediaContainer container = containerRepository.findByMasterMediaId(masterId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found for master: " + masterId));

        MediaFormat format = formatRepository.findById(formatId)
            .orElseThrow(() -> new IllegalArgumentException("Format not found: " + formatId));

        Media variant = mediaRepository.findById(variantMediaId)
            .orElseThrow(() -> new IllegalArgumentException("Variant media not found: " + variantMediaId));

        boolean exists = container.getItems().stream()
            .anyMatch(item -> item.getFormat().getId().equals(formatId));

        if (exists) {
            log.debug("Format {} already exists in container {}", format.getCode(), container.getCode());
            return;
        }

        MediaContainerItem item = new MediaContainerItem();
        item.setContainer(container);
        item.setFormat(format);
        item.setMedia(variant);

        container.getItems().add(item);
        containerRepository.save(container);

        log.debug("Added variant {} with format {} to container {}",
            variantMediaId, format.getCode(), container.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaContainer> getByMasterMediaId(Long masterMediaId) {
        return containerRepository.findByMasterMediaId(masterMediaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> getVariant(Long masterId, String formatCode) {
        return containerRepository.findByMasterMediaId(masterId)
            .map(container -> container.getMediaForFormat(formatCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaContainer> findById(Long id) {
        return containerRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        containerRepository.deleteById(id);
        log.info("Deleted container {}", id);
    }
}
