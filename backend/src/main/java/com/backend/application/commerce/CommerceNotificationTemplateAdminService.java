package com.backend.application.commerce;

import java.util.List;

import com.backend.application.commerce.dto.CommerceNotificationTemplateCommand;
import com.backend.application.commerce.dto.CommerceNotificationTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceNotificationTemplateResponse;
import com.backend.domain.commerce.CommerceNotificationEventType;

public interface CommerceNotificationTemplateAdminService {

	List<CommerceNotificationTemplateResponse> listTemplates(
			CommerceNotificationEventType eventType,
			String language,
			Boolean active);

	CommerceNotificationTemplateResponse getTemplate(String templateUid);

	CommerceNotificationTemplateResponse updateTemplate(
			String templateUid,
			CommerceNotificationTemplateCommand command);

	CommerceNotificationTemplatePreviewResponse previewTemplate(String templateUid);
}
