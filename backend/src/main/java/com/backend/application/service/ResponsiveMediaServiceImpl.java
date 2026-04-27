package com.backend.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.dto.request.ResponsiveMediaRequest;
import com.backend.application.dto.response.MediaLinkedComponentUsageLinkTypeResponse;
import com.backend.application.dto.response.MediaLinkedComponentUsageResponse;
import com.backend.application.dto.response.ResponsiveMediaResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentMediaLink;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentMediaLinkRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for ResponsiveMediaSet operations.
 */
@Service
@RequiredArgsConstructor
public class ResponsiveMediaServiceImpl implements ResponsiveMediaService {

  private static final Logger log = LoggerFactory.getLogger(ResponsiveMediaServiceImpl.class);

  private final ResponsiveMediaSetRepository repository;
  private final MediaRepository mediaRepository;
  private final ComponentMediaLinkRepository linkRepository;
  private final ComponentRepository componentRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final ComponentEntryI18nRepository componentEntryI18nRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ComponentMediaLinkSyncService componentMediaLinkSyncService;

  @Override
  @Transactional
  public ResponsiveMediaResponse create(ResponsiveMediaRequest request) {
    String code = (request.code() != null && !request.code().isBlank())
        ? request.code()
        : UUID.randomUUID().toString().replace("-", "");

    log.info("Creating responsive media set with code: {}", code);

    if (repository.existsByCode(code)) {
      throw new IllegalArgumentException("Responsive media set with code already exists: " + code);
    }

    if (request.desktopMediaId() == null && request.mobileMediaId() == null) {
      throw new IllegalArgumentException("At least one media (desktop or mobile) must be provided");
    }

    ResponsiveMediaSet entity = new ResponsiveMediaSet();
    entity.setCode(code);

    // Set desktop media if provided
    if (request.desktopMediaId() != null) {
      Media desktopMedia = mediaRepository.findById(request.desktopMediaId())
          .orElseThrow(() -> new EntityNotFoundException("Media", request.desktopMediaId()));
      entity.setDesktopMedia(desktopMedia);
    }

    // Set mobile media if provided
    if (request.mobileMediaId() != null) {
      Media mobileMedia = mediaRepository.findById(request.mobileMediaId())
          .orElseThrow(() -> new EntityNotFoundException("Media", request.mobileMediaId()));
      entity.setMobileMedia(mobileMedia);
    }

    ResponsiveMediaSet saved = repository.save(entity);
    log.info("Created responsive media set with id: {}", saved.getId());

    return ResponsiveMediaResponse.from(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponsiveMediaResponse getById(Long id) {
    ResponsiveMediaSet entity = getEntityById(id);
    return ResponsiveMediaResponse.from(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponsiveMediaResponse getByUid(String uid) {
    ResponsiveMediaSet entity = repository.findByUid(uid)
        .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", uid));
    return ResponsiveMediaResponse.from(entity);
  }

  @Override
  @Transactional
  public ResponsiveMediaResponse update(Long id, ResponsiveMediaRequest request) {
    log.info("Updating responsive media set: {}", id);

    ResponsiveMediaSet entity = getEntityById(id);

    if (request.code() != null && !request.code().isBlank() && !entity.getCode().equals(request.code())) {
      if (repository.existsByCode(request.code())) {
        throw new IllegalArgumentException("Responsive media set with code already exists: " + request.code());
      }
      entity.setCode(request.code());
    }

    // Update desktop media
    if (request.desktopMediaId() != null) {
      Media desktopMedia = mediaRepository.findById(request.desktopMediaId())
          .orElseThrow(() -> new EntityNotFoundException("Media", request.desktopMediaId()));
      entity.setDesktopMedia(desktopMedia);
    } else {
      entity.setDesktopMedia(null);
    }

    // Update mobile media
    if (request.mobileMediaId() != null) {
      Media mobileMedia = mediaRepository.findById(request.mobileMediaId())
          .orElseThrow(() -> new EntityNotFoundException("Media", request.mobileMediaId()));
      entity.setMobileMedia(mobileMedia);
    } else {
      entity.setMobileMedia(null);
    }

    // Validate at least one media remains after update
    if (entity.getDesktopMedia() == null && entity.getMobileMedia() == null) {
      throw new IllegalArgumentException("At least one media (desktop or mobile) must be assigned");
    }

    ResponsiveMediaSet saved = repository.save(entity);
    log.info("Updated responsive media set: {}", id);

    componentMediaLinkSyncService.rebuildForResponsiveSet(saved);

    return ResponsiveMediaResponse.from(saved);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("Deleting responsive media set: {}", id);

    ResponsiveMediaSet entity = getEntityById(id);

    // Delete all media links associated with this responsive set
    linkRepository.deleteByResponsiveSetId(id);

    repository.delete(entity);
    log.info("Deleted responsive media set: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponsiveMediaDeliveryResponse toDeliveryResponse(ResponsiveMediaSet entity, Language language) {
    return ResponsiveMediaDeliveryResponse.from(entity, language);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MediaLinkedComponentUsageResponse> getLinkedComponentUsages(Long mediaId) {
    log.info("Getting linked components for mediaId: {}", mediaId);
    try {
      List<ComponentMediaLink> links = linkRepository.findByMediaId(mediaId);
      log.info("Found {} links for mediaId: {}", links.size(), mediaId);

      if (links.isEmpty()) {
        return List.of();
      }

      List<Long> componentIds = links.stream()
          .map(ComponentMediaLink::getComponentId)
          .distinct()
          .toList();
      Map<Long, Component> componentById = componentRepository.findByIdIn(componentIds).stream()
          .collect(Collectors.toMap(Component::getId, Function.identity()));

      List<Long> componentTypeIds = componentById.values().stream()
          .map(Component::getComponentTypeId)
          .distinct()
          .toList();
      Map<Long, String> componentTypeNamesById = componentTypeIds.isEmpty()
          ? Map.of()
          : componentTypeRepository.findByIdIn(componentTypeIds).stream()
              .collect(Collectors.toMap(ComponentType::getId, ComponentType::getUid));

      List<Long> entryIds = links.stream()
          .map(ComponentMediaLink::getEntryId)
          .filter(entryId -> entryId != null)
          .distinct()
          .toList();
      Map<Long, ComponentEntry> entryById = entryIds.isEmpty()
          ? Map.of()
          : componentEntryRepository.findByIdIn(entryIds).stream()
              .collect(Collectors.toMap(ComponentEntry::getId, Function.identity()));
      Map<Long, List<ComponentEntryI18n>> entryI18nByEntryId = entryIds.isEmpty()
          ? Map.of()
          : componentEntryI18nRepository.findByEntryIdIn(entryIds).stream()
              .collect(Collectors.groupingBy(ComponentEntryI18n::getEntryId));

      Map<LinkedUsageTarget, List<ComponentMediaLink>> linksByTarget = links.stream()
          .collect(Collectors.groupingBy(
              link -> new LinkedUsageTarget(link.getComponentId(), link.getEntryId()),
              LinkedHashMap::new,
              Collectors.toList()));

      return linksByTarget.values().stream()
          .map(groupedLinks -> {
            ComponentMediaLink link = groupedLinks.get(0);
            Component component = componentById.get(link.getComponentId());
            ComponentEntry entry = link.getEntryId() != null ? entryById.get(link.getEntryId()) : null;
            String componentTypeName = component != null
                ? componentTypeNamesById.get(component.getComponentTypeId())
                : null;
            String entryTitle = entry != null ? resolveEntryTitle(entryI18nByEntryId.get(entry.getId())) : null;
            List<MediaLinkedComponentUsageLinkTypeResponse> linkTypes = groupedLinks.stream()
                .map(groupedLink -> toLinkedUsageLinkType(groupedLink.getLinkType()))
                .distinct()
                .toList();
            Long responsiveSetId = groupedLinks.stream()
                .map(ComponentMediaLink::getResponsiveSetId)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);

            return new MediaLinkedComponentUsageResponse(
                link.getComponentId(),
                component != null ? component.getUid() : null,
                component != null ? component.getName() : null,
                resolveComponentLabel(link, component),
                componentTypeName,
                link.getEntryId(),
                entry != null ? entry.getUid() : null,
                entry != null ? entry.getSortOrder() : null,
                entryTitle,
                resolveEntryLabel(link, entry, entryTitle),
                linkTypes,
                responsiveSetId);
          })
          .toList();
    } catch (Exception e) {
      log.error("Error getting linked components for mediaId: {}", mediaId, e);
      throw e;
    }
  }

  private String resolveEntryTitle(List<ComponentEntryI18n> translations) {
    if (translations == null || translations.isEmpty()) {
      return null;
    }

    return translations.stream()
        .map(ComponentEntryI18n::getTitle)
        .filter(title -> title != null && !title.isBlank())
        .findFirst()
        .orElse(null);
  }

  private String resolveComponentLabel(ComponentMediaLink link, Component component) {
    if (component == null) {
      return "#" + link.getComponentId();
    }

    if (component.getUid() != null && !component.getUid().isBlank()) {
      return component.getUid();
    }

    return component.getName() != null && !component.getName().isBlank()
        ? component.getName()
        : "#" + link.getComponentId();
  }

  private String resolveEntryLabel(ComponentMediaLink link, ComponentEntry entry, String entryTitle) {
    if (link.getEntryId() == null) {
      return null;
    }

    if (entry == null) {
      return "#" + link.getEntryId();
    }

    if (entry.getUid() != null && !entry.getUid().isBlank()) {
      return entry.getUid();
    }

    return entryTitle != null && !entryTitle.isBlank()
        ? entryTitle
        : "#" + link.getEntryId();
  }

  private MediaLinkedComponentUsageLinkTypeResponse toLinkedUsageLinkType(ComponentMediaLink.LinkType linkType) {
    String code = linkType.name();
    return new MediaLinkedComponentUsageLinkTypeResponse(
        code,
        code.replace("RESPONSIVE_", "").toLowerCase(java.util.Locale.ROOT));
  }

  private record LinkedUsageTarget(Long componentId, Long entryId) {
  }

  @Override
  @Transactional(readOnly = true)
  public ResponsiveMediaSet getEntityById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", id));
  }
}
