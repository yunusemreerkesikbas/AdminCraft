package com.backend.application.cms.preview;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.enums.CmsDraftTargetType;

public record SmartEditDraftItemResponse(
    Long draftId,
    CmsDraftTargetType targetType,
    Long targetId,
    String language,
    Long componentId,
    String componentUid,
    String componentName,
    Long entryId,
    String entryUid,
    List<SmartEditDraftFieldChange> fieldChanges,
    LocalDateTime updatedAt,
    Long updatedBy) {
}
