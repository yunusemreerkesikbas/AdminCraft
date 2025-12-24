package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ComponentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EntryI18nRequest(
        @Size(max = 255, message = "Title must be at most 255 characters") String title,

        @Size(max = 5000, message = "Description must be at most 5000 characters") String description,

        @NotNull(message = "Status is required") ComponentStatus status,

        @Size(max = 50, message = "Dynamic fields map cannot exceed 50 entries") Map<String, Object> dynamicFields) {
}
