package com.backend.application.dto.request;

import java.util.Map;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EntryI18nRequest(
        @Size(max = 255, message = "{validation.component.entry.title.size}") String title,

        @Size(max = 5000, message = "{validation.component.entry.description.size}") String description,

        @Size(max = 50, message = "{validation.component.entry.dynamic.fields.size}") Map<String, Object> dynamicFields) {
}
