package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.dto.ImageDimensions;
import com.backend.application.dto.request.MediaBindRequest;
import com.backend.application.dto.response.BulkDeleteResultResponse;
import com.backend.application.support.BulkDeleteExceptionMapper;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentMediaLink;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentMediaLinkRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.presentation.dto.request.MediaI18nRequest;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaI18nService i18nService;
    private final MediaStorageService storageService;
    private final MediaProcessingService processingService;
    private final MediaContainerService containerService;
    private final StorageConfigProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final ResponsiveMediaService responsiveMediaService;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final SiteActivityPublisher activityPublisher;
    private final ComponentService componentService;
    private final ComponentEntryRepository componentEntryRepository;
    private final ComponentMediaLinkSyncService componentMediaLinkSyncService;
    private final ComponentMediaLinkRepository componentMediaLinkRepository;
    private final MessageSource messageSource;
    private final SecurityHelper securityHelper;
    @Qualifier("tenantTransactionManager")
    private final PlatformTransactionManager tenantTransactionManager;

    private TransactionTemplate requiresNewTx;

    @PostConstruct
    void initRequiresNewTx() {
        requiresNewTx = new TransactionTemplate(tenantTransactionManager);
        requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private void deleteInNewTransaction(Long id) {
        AtomicReference<String> filePathRef = new AtomicReference<>();
        requiresNewTx.execute(status -> {
            Media media = mediaRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Media", id));
            filePathRef.set(media.getFilePath());
            mediaRepository.deleteById(id);
            log.info("Media deleted from database: {}", id);
            activityPublisher.publishMediaEvent(id, media.getOriginalName(),
                    ActivityAction.DELETED, securityHelper.getCurrentUserIdOrNull(), null, null);
            return null;
        });
        String filePath = filePathRef.get();
        if (filePath != null) {
            try {
                storageService.delete(filePath);
            } catch (Exception e) {
                log.warn("Failed to delete file {} after DB commit: {}", filePath, e.getMessage());
            }
        }
    }

    @Override
    public Media uploadComposite(MultipartFile file, Long uploadedBy,
            Map<Language, MediaI18nRequest> translations) {
        Media media = uploadFile(file, uploadedBy);

        return transactionTemplate.execute(status -> {
            Media currentMedia = mediaRepository.findById(media.getId()).orElseThrow();

            Media saved = mediaRepository.save(currentMedia);

            if (translations != null && !translations.isEmpty()) {
                translations.forEach((lang, req) -> {
                    i18nService.upsert(saved.getId(), lang, req.altText(), req.title(), req.description());
                });
            }
            return saved;
        });
    }

    @Override
    public Media uploadFile(MultipartFile file, Long uploadedBy) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        MediaStorageService.ValidationResult validation = storageService.validate(file);
        if (!validation.valid()) {
            throw validation.toUploadException();
        }

        MediaStorageService.StoredFileResult stored = storageService.store(file, "media");

        ImageDimensions dimensions = null;
        boolean requiresProcessing = processingService.isProcessingSupported(stored.mimeType());
        if (requiresProcessing) {
            byte[] content = storageService.retrieve(stored.filePath());
            dimensions = processingService.extractDimensions(content);
        }

        final ImageDimensions finalDimensions = dimensions;

        try {
            Media savedMedia = transactionTemplate.execute(status -> {
                Media media = new Media();
                media.setOriginalName(file.getOriginalFilename());
                media.setFileName(stored.fileName());
                media.setFilePath(stored.filePath());
                media.setMimeType(stored.mimeType());
                media.setFileSize(stored.fileSize());
                media.setFileExtension(stored.extension());
                media.setUploadedBy(uploadedBy);
                StorageProvider storageProvider = StorageProvider.valueOf(properties.getProvider().toUpperCase());
                media.setStorageProvider(storageProvider);
                if (storageProvider == StorageProvider.S3) {
                    media.setExternalUrl(storageService.getPublicUrl(stored.filePath()));
                }
                media.setIsPublic(true);
                media.setUsageCount(0);

                media.setStatus(MediaStatus.ACTIVE);
                if (requiresProcessing && finalDimensions != null) {
                    media.setWidth(finalDimensions.width());
                    media.setHeight(finalDimensions.height());
                }

                Media saved = mediaRepository.save(media);
                log.info("File uploaded: {} with UID: {}", stored.fileName(), saved.getUid());
                containerService.createForMedia(saved.getId());
                return saved;
            });

            activityPublisher.publishMediaEvent(savedMedia.getId(), savedMedia.getOriginalName(),
                    ActivityAction.UPLOADED, uploadedBy, null, null);
            return savedMedia;
        } catch (Exception e) {
            log.error("Failed to save media metadata, rolling back file storage: {}", stored.filePath());
            storageService.delete(stored.filePath());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> findById(Long id) {
        return mediaRepository.findById(id);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Optional<Media> findByUid(String uid) {
        return mediaRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Media> findByFileName(String fileName) {
        return mediaRepository.findByFileName(fileName);
    }

    @Override
    @Transactional
    public Media update(Media media) {
        log.debug("Updating media with UID: {}", media.getUid());

        if (!mediaRepository.existsById(media.getId())) {
            throw new IllegalArgumentException("Media not found with ID: " + media.getId());
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media updated: {}", updatedMedia.getUid());
        return updatedMedia;
    }

    @Override
    @Transactional
    public Media updateMetadata(Long id, Boolean isPublic, List<String> tags) {
        log.debug("Updating media metadata for ID: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        if (isPublic != null) {
            media.setIsPublic(isPublic);
        }

        if (tags != null) {
            media.setTags(String.join(",", tags));
        }

        Media updatedMedia = mediaRepository.save(media);
        log.info("Media metadata updated: {}", updatedMedia.getUid());
        return updatedMedia;
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting media with ID: {}", id);
        deleteInNewTransaction(id);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public BulkDeleteResultResponse bulkDeleteMedia(List<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        List<BulkDeleteResultResponse.BulkDeleteError> errors = new ArrayList<>();

        for (Long id : ids) {
            try {
                deleteInNewTransaction(id);
                deletedIds.add(id);
            } catch (Exception ex) {
                failedIds.add(id);
                errors.add(BulkDeleteExceptionMapper.toError(id, ex, messageSource));
            }
        }

        return new BulkDeleteResultResponse(ids.size(), deletedIds, failedIds, errors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> findAll() {
        return mediaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Media> findAll(Pageable pageable) {
        return mediaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Media> search(Pageable pageable, String searchQuery) {
        // If search query is null/empty or less than 2 chars, return all
        if (searchQuery == null || searchQuery.trim().length() < 2) {
            return mediaRepository.findAll(pageable);
        }
        return mediaRepository.searchByQuery(searchQuery.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFileContent(String fileName) {
        Media media = mediaRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        return storageService.retrieve(media.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String getFileUrl(Long id) {
        return "/api/media/files/" + id;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return mediaRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long sumFileSize() {
        return mediaRepository.sumFileSize();
    }

    @Override
    public boolean isValidFileType(MultipartFile file) {
        return storageService.isValidMimeType(file.getContentType());
    }

    @Override
    public boolean isFileSizeAllowed(MultipartFile file, Long maxSize) {
        return storageService.isValidFileSize(file.getSize());
    }

    @Override
    public List<String> getAllowedExtensions() {
        return properties.getAllowedMimeTypes().stream()
                .map(mime -> mime.substring(mime.lastIndexOf('/') + 1))
                .toList();
    }

    @Override
    @Transactional
    public void updateFocalPoint(Long id, Double x, Double y) {
        log.debug("Updating focal point for media ID: {} to ({}, {})", id, x, y);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + id));

        media.setFocalPointX(x);
        media.setFocalPointY(y);
        mediaRepository.save(media);

        log.info("Focal point updated for media {}: ({}, {})", media.getUid(), x, y);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> findByUids(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return List.of();
        }
        return mediaRepository.findByUidIn(uids);
    }

    @Override
    @Transactional
    public void bindMedia(Long mediaId, MediaBindRequest request) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media", mediaId));

        if (request.targetType() == MediaBindRequest.TargetType.COMPONENT) {
            bindComponentMedia(media, request.targetId(), request.responsiveTarget());
            return;
        }

        bindEntryMedia(media, request.targetId(), request.responsiveTarget());
    }

    @Override
    @Transactional
    public void unlinkMedia(Long mediaId, Long componentId, Long entryId, String linkType) {
        mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media", mediaId));

        ComponentMediaLink.LinkType resolvedLinkType = resolveLinkType(linkType);
        ComponentMediaLink link = componentMediaLinkRepository.findByMediaId(mediaId).stream()
                .filter(candidate -> componentId.equals(candidate.getComponentId())
                        && Objects.equals(entryId, candidate.getEntryId())
                        && resolvedLinkType == candidate.getLinkType())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "ComponentMediaLink",
                        "mediaId=%d, componentId=%d, entryId=%s, linkType=%s".formatted(
                                mediaId,
                                componentId,
                                entryId != null ? entryId : "null",
                                resolvedLinkType.name())));

        if (link.getLinkType() == ComponentMediaLink.LinkType.ENTRY_MEDIA || link.getResponsiveSetId() == null) {
            throw new IllegalArgumentException("Legacy media links cannot be removed from the media detail dialog");
        }

        ResponsiveMediaSet responsiveSet = responsiveMediaSetRepository.findById(link.getResponsiveSetId())
                .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", link.getResponsiveSetId()));

        Long nextDesktopMediaId = responsiveSet.getDesktopMedia() != null
                ? responsiveSet.getDesktopMedia().getId()
                : null;
        Long nextMobileMediaId = responsiveSet.getMobileMedia() != null
                ? responsiveSet.getMobileMedia().getId()
                : null;

        if (resolvedLinkType == ComponentMediaLink.LinkType.RESPONSIVE_DESKTOP) {
            nextDesktopMediaId = null;
        } else if (resolvedLinkType == ComponentMediaLink.LinkType.RESPONSIVE_MOBILE) {
            nextMobileMediaId = null;
        }

        if (nextDesktopMediaId == null && nextMobileMediaId == null) {
            clearResponsiveMediaTarget(componentId, entryId);
            return;
        }

        responsiveMediaService.update(
                responsiveSet.getId(),
                new com.backend.application.dto.request.ResponsiveMediaRequest(
                        responsiveSet.getCode(),
                        nextDesktopMediaId,
                        nextMobileMediaId));
    }

    private void bindComponentMedia(
            Media media,
            Long componentId,
            MediaBindRequest.ResponsiveTarget responsiveTarget
    ) {
        Component component = componentService.getComponentById(componentId);
        Long responsiveSetId = ensureResponsiveMediaSet(
                media,
                component.getResponsiveMedia(),
                responsiveTarget
        );
        componentService.assignResponsiveMedia(componentId, responsiveSetId);
    }

    private void bindEntryMedia(
            Media media,
            Long entryId,
            MediaBindRequest.ResponsiveTarget responsiveTarget
    ) {
        ComponentEntry entry = componentEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("ComponentEntry", entryId));

        Long responsiveSetId = ensureResponsiveMediaSet(
                media,
                entry.getResponsiveMedia(),
                responsiveTarget
        );
        if (entry.getResponsiveMedia() == null || !responsiveSetId.equals(entry.getResponsiveMedia().getId())) {
            ResponsiveMediaSet responsiveMediaSet = responsiveMediaSetRepository.findById(responsiveSetId)
                    .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", responsiveSetId));
            entry.setResponsiveMedia(responsiveMediaSet);
            entry = componentEntryRepository.save(entry);
        }

        componentMediaLinkSyncService.syncEntryResponsiveLinks(entry);
    }

    private Long ensureResponsiveMediaSet(
            Media media,
            ResponsiveMediaSet existingSet,
            MediaBindRequest.ResponsiveTarget requestedTarget
    ) {
        MediaBindRequest.ResponsiveTarget target = requestedTarget != null
                ? requestedTarget
                : MediaBindRequest.ResponsiveTarget.DESKTOP;
        Long desktopMediaId = shouldBindDesktop(target)
                ? media.getId()
                : existingSet != null && existingSet.getDesktopMedia() != null
                        ? existingSet.getDesktopMedia().getId()
                        : null;
        Long mobileMediaId = shouldBindMobile(target)
                ? media.getId()
                : existingSet != null && existingSet.getMobileMedia() != null
                        ? existingSet.getMobileMedia().getId()
                        : null;

        if (existingSet == null) {
            return responsiveMediaService.create(new com.backend.application.dto.request.ResponsiveMediaRequest(
                    UUID.randomUUID().toString().replace("-", ""),
                    desktopMediaId,
                    mobileMediaId)).id();
        }

        return responsiveMediaService.update(existingSet.getId(),
                new com.backend.application.dto.request.ResponsiveMediaRequest(
                        existingSet.getCode(),
                        desktopMediaId,
                        mobileMediaId)).id();
    }

    private boolean shouldBindDesktop(MediaBindRequest.ResponsiveTarget target) {
        return target == MediaBindRequest.ResponsiveTarget.DESKTOP
                || target == MediaBindRequest.ResponsiveTarget.BOTH;
    }

    private boolean shouldBindMobile(MediaBindRequest.ResponsiveTarget target) {
        return target == MediaBindRequest.ResponsiveTarget.MOBILE
                || target == MediaBindRequest.ResponsiveTarget.BOTH;
    }

    private ComponentMediaLink.LinkType resolveLinkType(String linkType) {
        try {
            return ComponentMediaLink.LinkType.valueOf(linkType.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported media link type: " + linkType);
        }
    }

    private void clearResponsiveMediaTarget(Long componentId, Long entryId) {
        if (entryId != null) {
            ComponentEntry entry = componentEntryRepository.findById(entryId)
                    .orElseThrow(() -> new EntityNotFoundException("ComponentEntry", entryId));
            entry.setResponsiveMedia(null);
            ComponentEntry savedEntry = componentEntryRepository.save(entry);
            componentMediaLinkSyncService.syncEntryResponsiveLinks(savedEntry);
            return;
        }

        componentService.assignResponsiveMedia(componentId, null);
    }

    @Override
    public String resolvePublicUrl(String mediaUid) {
        if (mediaUid == null || mediaUid.isBlank()) {
            return null;
        }
        return findByUid(mediaUid)
                .map(media -> {
                    String external = media.getPublicUrl();
                    if (external != null && !external.isBlank()) {
                        return external;
                    }
                    return "/api/media/files/" + media.getFileName();
                })
                .orElse(null);
    }

}
