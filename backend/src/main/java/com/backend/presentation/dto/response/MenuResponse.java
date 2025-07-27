package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.List;

public record MenuResponse(
    Long id,
    String name,
    Language language,
    Long tenantId,
    Long siteId,
    List<MenuItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

record MenuItemResponse(
    Long id,
    String title,
    String url,
    Integer order,
    Long parentId,
    Boolean isActive,
    String target,
    String cssClass,
    String icon
) {}