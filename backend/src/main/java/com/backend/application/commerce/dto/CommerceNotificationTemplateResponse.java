package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceNotificationTemplate;

public record CommerceNotificationTemplateResponse(
		String templateUid,
		String eventType,
		String channel,
		String language,
		String subject,
		String content,
		Boolean active,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static CommerceNotificationTemplateResponse from(CommerceNotificationTemplate template) {
		return new CommerceNotificationTemplateResponse(
				template.getUid(),
				template.getTemplateKey().name(),
				template.getChannel().name(),
				template.getLanguage(),
				template.getSubject(),
				template.getContent(),
				template.getActive(),
				template.getCreatedAt(),
				template.getUpdatedAt());
	}
}
