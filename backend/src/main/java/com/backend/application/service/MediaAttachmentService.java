package com.backend.application.service;

import com.backend.application.dto.media.MediaUsageDto;
import com.backend.domain.enums.MediaPurpose;
import java.util.List;

public interface MediaAttachmentService {
  List<MediaUsageDto> attach(Long tenantId, String ownerType, Long ownerId,
      List<Long> mediaIds, MediaPurpose purpose);

  MediaUsageDto setCover(Long tenantId, String ownerType, Long ownerId,
      Long usageId);

  List<MediaUsageDto> reorder(Long tenantId, String ownerType, Long ownerId,
      List<Long> orderedUsageIds);

  void detach(Long tenantId, String ownerType, Long ownerId, Long usageId);
}
