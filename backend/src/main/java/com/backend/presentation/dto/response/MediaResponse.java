package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

public record MediaResponse(
    Long id,
    String originalName,
    String fileName,
    String filePath,
    String mimeType,
    Long fileSize,
    Integer width,
    Integer height,
    String altTextTr,
    String altTextEn,
    String descriptionTr,
    String descriptionEn,
    String titleTr,
    String titleEn,
    String folder,
    String category,
    String tags,
    Boolean isPublic,
    Long usageCount,
    String fileUrl,
    String thumbnailUrl,
    Long tenantId,
    Long uploadedBy,
    String uploaderName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}