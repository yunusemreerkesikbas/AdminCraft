package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComponentRequest(
        @NotNull Long tenantId,
        @NotNull ComponentType type,
        @NotBlank @Size(max = 100) @Pattern(regexp = "^[a-z0-9._-]+$") String key,
        ComponentStatus status,
        Boolean visible,
        Integer sortOrder,
        @Size(max = 200, message = "component.title.max.length") String titleTr,
        @Size(max = 300, message = "component.subtitle.max.length") String subtitleTr,
        String dataTr,
        @Size(max = 200, message = "component.title.max.length") String titleEn,
        @Size(max = 300, message = "component.subtitle.max.length") String subtitleEn,
        String dataEn) {

    // Removed business logic - defaults are now handled in Domain Entity
    // Clean DTO focusing only on data transfer and validation
}