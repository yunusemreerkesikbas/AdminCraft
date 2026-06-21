package com.backend.presentation.commerce;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceNotificationTemplateAdminService;
import com.backend.application.commerce.dto.CommerceNotificationTemplateCommand;
import com.backend.application.commerce.dto.CommerceNotificationTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceNotificationTemplateResponse;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/admin/notifications/templates")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Validated
@RequiredArgsConstructor
public class CommerceAdminNotificationTemplateController {

	private final CommerceNotificationTemplateAdminService templateAdminService;
	private final MessageSource messageSource;

	@GetMapping
	public ResponseEntity<ApiResponse<List<CommerceNotificationTemplateResponse>>> list(
			@RequestParam(required = false) CommerceNotificationEventType eventType,
			@RequestParam(required = false) String language,
			@RequestParam(required = false) Boolean active) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.notification.templates.retrieved"),
				templateAdminService.listTemplates(eventType, language, active)));
	}

	@GetMapping("/{templateUid}")
	public ResponseEntity<ApiResponse<CommerceNotificationTemplateResponse>> get(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.notification.template.retrieved"),
				templateAdminService.getTemplate(templateUid)));
	}

	@PutMapping("/{templateUid}")
	public ResponseEntity<ApiResponse<CommerceNotificationTemplateResponse>> update(
			@PathVariable @Uid String templateUid,
			@Valid @RequestBody CommerceNotificationTemplateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.notification.template.updated"),
				templateAdminService.updateTemplate(
						templateUid,
						new CommerceNotificationTemplateCommand(
								request.subject(),
								request.content(),
								request.active()))));
	}

	@GetMapping("/{templateUid}/preview")
	public ResponseEntity<ApiResponse<CommerceNotificationTemplatePreviewResponse>> preview(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.notification.template.previewed"),
				templateAdminService.previewTemplate(templateUid)));
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
