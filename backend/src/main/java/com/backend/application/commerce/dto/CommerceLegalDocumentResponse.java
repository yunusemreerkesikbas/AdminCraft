package com.backend.application.commerce.dto;

import com.backend.domain.commerce.CommerceLegalTemplateType;

public record CommerceLegalDocumentResponse(
		String templateUid,
		CommerceLegalTemplateType type,
		String language,
		Integer version,
		String title,
		String contentText,
		String contentHash) {
}
