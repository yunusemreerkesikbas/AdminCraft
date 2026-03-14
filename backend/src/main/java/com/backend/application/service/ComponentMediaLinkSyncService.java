package com.backend.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentMediaLink;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentMediaLinkRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentMediaLinkSyncService {

  private final ComponentMediaLinkRepository linkRepository;
  private final ComponentRepository componentRepository;
  private final ComponentEntryRepository componentEntryRepository;
  private final MediaRepository mediaRepository;

  @Transactional
  public void syncComponentResponsiveLinks(Component component) {
    if (component == null || component.getId() == null) {
      return;
    }

    Set<Long> affectedMediaIds = new LinkedHashSet<>(collectMediaIds(linkRepository.findByComponentId(component.getId())));
    collectResponsiveMediaIds(affectedMediaIds, component.getResponsiveMedia());

    linkRepository.deleteComponentLevelByComponentId(component.getId());

    ResponsiveMediaSet responsiveMedia = component.getResponsiveMedia();
    if (responsiveMedia == null) {
      refreshUsageCounts(affectedMediaIds);
      return;
    }

    saveResponsiveLinks(component.getId(), null, responsiveMedia);
    refreshUsageCounts(affectedMediaIds);
  }

  @Transactional
  public void syncEntryResponsiveLinks(ComponentEntry entry) {
    if (entry == null || entry.getId() == null) {
      return;
    }

    Set<Long> affectedMediaIds = new LinkedHashSet<>(collectMediaIds(linkRepository.findByEntryId(entry.getId())));
    collectResponsiveMediaIds(affectedMediaIds, entry.getResponsiveMedia());

    linkRepository.deleteByEntryId(entry.getId());

    ResponsiveMediaSet responsiveMedia = entry.getResponsiveMedia();
    if (responsiveMedia == null) {
      refreshUsageCounts(affectedMediaIds);
      return;
    }

    saveResponsiveLinks(entry.getComponentId(), entry.getId(), responsiveMedia);
    refreshUsageCounts(affectedMediaIds);
  }

  @Transactional
  public void removeEntryResponsiveLinks(Long entryId) {
    if (entryId == null) {
      return;
    }
    Set<Long> affectedMediaIds = new LinkedHashSet<>(collectMediaIds(linkRepository.findByEntryId(entryId)));
    linkRepository.deleteByEntryId(entryId);
    refreshUsageCounts(affectedMediaIds);
  }

  @Transactional
  public void removeComponentResponsiveLinks(Long componentId) {
    if (componentId == null) {
      return;
    }
    Set<Long> affectedMediaIds = new LinkedHashSet<>(collectMediaIds(linkRepository.findByComponentId(componentId)));
    linkRepository.deleteByComponentId(componentId);
    refreshUsageCounts(affectedMediaIds);
  }

  @Transactional
  public void rebuildForResponsiveSet(ResponsiveMediaSet responsiveSet) {
    if (responsiveSet == null || responsiveSet.getId() == null) {
      return;
    }

    List<Component> components = componentRepository.findByResponsiveMediaId(responsiveSet.getId());
    components.forEach(this::syncComponentResponsiveLinks);

    List<ComponentEntry> entries = componentEntryRepository.findByResponsiveMediaId(responsiveSet.getId());
    entries.forEach(this::syncEntryResponsiveLinks);

    log.info(
        "Rebuilt media links for responsive set {}: components={}, entries={}",
        responsiveSet.getId(),
        components.size(),
        entries.size());
  }

  private void saveResponsiveLinks(Long componentId, Long entryId, ResponsiveMediaSet responsiveSet) {
    if (responsiveSet.getDesktopMedia() != null) {
      saveLinkIfMissing(buildLink(componentId, entryId, responsiveSet, true));
    }

    if (responsiveSet.getMobileMedia() != null) {
      saveLinkIfMissing(buildLink(componentId, entryId, responsiveSet, false));
    }
  }

  private ComponentMediaLink buildLink(
      Long componentId,
      Long entryId,
      ResponsiveMediaSet responsiveSet,
      boolean isDesktop) {
    if (entryId == null) {
      return ComponentMediaLink.forComponentResponsive(
          componentId,
          isDesktop ? responsiveSet.getDesktopMedia().getId() : responsiveSet.getMobileMedia().getId(),
          responsiveSet.getId(),
          isDesktop);
    }

    return ComponentMediaLink.forEntryResponsive(
        componentId,
        entryId,
        isDesktop ? responsiveSet.getDesktopMedia().getId() : responsiveSet.getMobileMedia().getId(),
        responsiveSet.getId(),
        isDesktop);
  }

  private void saveLinkIfMissing(ComponentMediaLink link) {
    boolean exists = linkRepository.existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
        link.getComponentId(),
        link.getMediaId(),
        link.getLinkType(),
        link.getEntryId());
    if (!exists) {
      linkRepository.save(link);
    }
  }

  private List<Long> collectMediaIds(List<ComponentMediaLink> links) {
    return links.stream()
        .map(ComponentMediaLink::getMediaId)
        .distinct()
        .toList();
  }

  private void collectResponsiveMediaIds(Set<Long> mediaIds, ResponsiveMediaSet responsiveMedia) {
    if (responsiveMedia == null) {
      return;
    }
    if (responsiveMedia.getDesktopMedia() != null) {
      mediaIds.add(responsiveMedia.getDesktopMedia().getId());
    }
    if (responsiveMedia.getMobileMedia() != null) {
      mediaIds.add(responsiveMedia.getMobileMedia().getId());
    }
  }

  private void refreshUsageCounts(Set<Long> mediaIds) {
    if (mediaIds.isEmpty()) {
      return;
    }

    List<Media> mediaList = mediaRepository.findByIdIn(new ArrayList<>(mediaIds));
    for (Media media : mediaList) {
      media.setUsageCount(Math.toIntExact(linkRepository.countByMediaId(media.getId())));
      mediaRepository.save(media);
    }
  }
}
