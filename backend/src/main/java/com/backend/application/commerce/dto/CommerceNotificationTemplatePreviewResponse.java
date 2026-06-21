package com.backend.application.commerce.dto;

public record CommerceNotificationTemplatePreviewResponse(
		String templateUid,
		String subject,
		String content) {
}
