package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import jakarta.validation.constraints.Size;

public record UpdateComponentRequest(
    ComponentStatus status,
    Boolean visible,
    Integer sortOrder,
    @Size(max = 200) String titleTr,
    @Size(max = 300) String subtitleTr,
    String dataTr,
    @Size(max = 200) String titleEn,
    @Size(max = 300) String subtitleEn,
    String dataEn) {
}
