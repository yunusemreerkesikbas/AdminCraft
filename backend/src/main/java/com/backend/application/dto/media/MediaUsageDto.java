package com.backend.application.dto.media;

import com.backend.domain.enums.MediaPurpose;

public record MediaUsageDto(
    Long id,
    Long tenantId,
    String ownerType,
    Long ownerId,
    Long mediaId,
    MediaPurpose purpose,
    Boolean isCover,
    Integer sortOrder) {
}
