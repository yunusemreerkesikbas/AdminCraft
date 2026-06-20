package com.backend.presentation.commerce;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceLegalService;
import com.backend.application.commerce.dto.CommerceLegalTemplateCommand;
import com.backend.application.commerce.dto.CommerceLegalTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceLegalTemplateResponse;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/admin/legal-templates")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Validated
@RequiredArgsConstructor
public class CommerceAdminLegalTemplateController {

	private final CommerceLegalService legalService;
	private final MessageSource messageSource;

	@GetMapping
	public ResponseEntity<ApiResponse<List<CommerceLegalTemplateResponse>>> list(
			@RequestParam(required = false) CommerceLegalTemplateType type,
			@RequestParam(required = false) String language,
			@RequestParam(required = false) CommerceLegalTemplateStatus status) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.templates.retrieved"),
				legalService.listTemplates(type, language, status)));
	}

	@GetMapping("/{templateUid}")
	public ResponseEntity<ApiResponse<CommerceLegalTemplateResponse>> get(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.retrieved"),
				legalService.getTemplate(templateUid)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<CommerceLegalTemplateResponse>> create(
			@Valid @RequestBody CommerceLegalTemplateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.created"),
				legalService.createTemplate(toCommand(request))));
	}

	@PutMapping("/{templateUid}")
	public ResponseEntity<ApiResponse<CommerceLegalTemplateResponse>> update(
			@PathVariable @Uid String templateUid,
			@Valid @RequestBody CommerceLegalTemplateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.updated"),
				legalService.updateTemplate(templateUid, toCommand(request))));
	}

	@PatchMapping("/{templateUid}/publish")
	public ResponseEntity<ApiResponse<CommerceLegalTemplateResponse>> publish(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.published"),
				legalService.publishTemplate(templateUid)));
	}

	@PatchMapping("/{templateUid}/archive")
	public ResponseEntity<ApiResponse<CommerceLegalTemplateResponse>> archive(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.archived"),
				legalService.archiveTemplate(templateUid)));
	}

	@GetMapping("/{templateUid}/preview")
	public ResponseEntity<ApiResponse<CommerceLegalTemplatePreviewResponse>> preview(
			@PathVariable @Uid String templateUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.legal.template.previewed"),
				legalService.previewTemplate(templateUid)));
	}

	private CommerceLegalTemplateCommand toCommand(CommerceLegalTemplateRequest request) {
		return new CommerceLegalTemplateCommand(
				request.type(),
				request.language(),
				request.title(),
				request.contentText());
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
