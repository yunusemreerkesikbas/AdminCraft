package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ComponentStatus;

import lombok.Builder;

@Builder
public record EntryI18nRequest(
    String title,
    String description,
    ComponentStatus status,
    Map<String, Object> dynamicFields) {
}
