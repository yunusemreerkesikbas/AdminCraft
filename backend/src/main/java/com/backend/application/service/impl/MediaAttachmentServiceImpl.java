package com.backend.application.service.impl;

import com.backend.application.dto.media.MediaUsageDto;
import com.backend.application.service.MediaAttachmentService;
import com.backend.application.service.MediaService;
import com.backend.domain.entity.MediaFile;
import com.backend.domain.entity.MediaUsage;
import com.backend.domain.enums.MediaPurpose;
import com.backend.domain.repository.MediaUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaAttachmentServiceImpl implements MediaAttachmentService {

  private final MediaUsageRepository usageRepository;
  private final MediaService mediaService;

  @Override
  public List<MediaUsageDto> attach(Long tenantId, String ownerType, Long ownerId,
      List<Long> mediaIds, MediaPurpose purpose) {
    int startOrder = usageRepository
        .findByTenantIdAndOwnerOrdered(ownerType, tenantId, ownerId).size();
    AtomicInteger orderCounter = new AtomicInteger(startOrder);
    List<MediaUsage> saved = mediaIds.stream().map(mid -> {
      MediaUsage u = new MediaUsage();
      u.setTenantId(tenantId);
      u.setOwnerType(ownerType);
      u.setOwnerId(ownerId);
      u.setMediaId(mid);
      u.setPurpose(purpose);
      u.setSortOrder(orderCounter.incrementAndGet());
      
      // Sprint 7: Activate staged media files when first attached
      mediaService.getMediaFileById(mid).ifPresent(mediaFile -> {
        if (mediaFile.isStaged()) {
          mediaFile.activate();
          mediaService.updateMediaFile(mediaFile);
        }
      });
      
      return usageRepository.save(u);
    }).toList();
    return saved.stream().map(this::toDto).toList();
  }

  @Override
  public MediaUsageDto setCover(Long tenantId, String ownerType, Long ownerId,
      Long usageId) {
    usageRepository.findCoverByTenantIdAndOwner(ownerType, tenantId, ownerId)
        .ifPresent(u -> {
          u.setIsCover(false);
          usageRepository.save(u);
        });
    MediaUsage usage = usageRepository.findById(usageId)
        .orElseThrow(() -> new IllegalArgumentException("Usage not found"));
    usage.setIsCover(true);
    return toDto(usageRepository.save(usage));
  }

  @Override
  public List<MediaUsageDto> reorder(Long tenantId, String ownerType, Long ownerId,
      List<Long> orderedUsageIds) {
    List<MediaUsage> usages = usageRepository
        .findByTenantIdAndOwnerOrdered(ownerType, tenantId, ownerId);
    for (int i = 0; i < orderedUsageIds.size(); i++) {
      Long id = orderedUsageIds.get(i);
      int order = i;
      usages.stream().filter(u -> u.getId().equals(id)).findFirst()
          .ifPresent(u -> {
            u.setSortOrder(order);
            usageRepository.save(u);
          });
    }
    return usageRepository
        .findByTenantIdAndOwnerOrdered(ownerType, tenantId, ownerId)
        .stream().map(this::toDto).toList();
  }

  @Override
  public void detach(Long tenantId, String ownerType, Long ownerId, Long usageId) {
    usageRepository.deleteById(usageId);
  }

  private MediaUsageDto toDto(MediaUsage u) {
    return new MediaUsageDto(
        u.getId(), u.getTenantId(), u.getOwnerType(), u.getOwnerId(),
        u.getMediaId(), u.getPurpose(), u.getIsCover(), u.getSortOrder());
  }
}
