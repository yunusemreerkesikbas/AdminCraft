package com.backend.application.cms.preview;

import java.time.LocalDateTime;
import java.util.List;

public record SmartEditDraftGroupResponse(
    String key,
    List<Long> draftIds,
    String title,
    String subtitle,
    List<SmartEditDraftFieldChange> fields,
    LocalDateTime updatedAt) {
}
