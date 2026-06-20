package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceLegalTemplate;

public record CommerceLegalTemplateResponse(
		String templateUid,
		String type,
		String language,
		Integer version,
		String status,
		String title,
		String contentText,
		LocalDateTime publishedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static CommerceLegalTemplateResponse from(CommerceLegalTemplate template) {
		return new CommerceLegalTemplateResponse(
				template.getUid(),
				template.getType().name(),
				template.getLanguage(),
				template.getVersion(),
				template.getStatus().name(),
				template.getTitle(),
				template.getContentText(),
				template.getPublishedAt(),
				template.getCreatedAt(),
				template.getUpdatedAt());
	}
}
