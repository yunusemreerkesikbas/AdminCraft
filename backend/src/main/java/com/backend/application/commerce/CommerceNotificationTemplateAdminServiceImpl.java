package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CommerceNotificationTemplateCommand;
import com.backend.application.commerce.dto.CommerceNotificationTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceNotificationTemplateResponse;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceNotificationTemplateAdminServiceImpl implements CommerceNotificationTemplateAdminService {

	private static final String TEMPLATE_NOT_FOUND = "commerce.notification.template.not.found";
	private static final int SUBJECT_MAX_LENGTH = 255;
	private static final int CONTENT_MAX_LENGTH = 20000;

	private final CommerceNotificationTemplateRepository templateRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final TemplateVariableRenderer templateVariableRenderer;

	@Override
	@Transactional(readOnly = true)
	public List<CommerceNotificationTemplateResponse> listTemplates(
			CommerceNotificationEventType eventType,
			CommerceNotificationChannel channel,
			String language,
			Boolean active) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return templateRepository.findAll(
						eventType,
						Objects.requireNonNullElse(channel, CommerceNotificationChannel.EMAIL),
						normalizeOptionalLanguage(language),
						active)
				.stream()
				.map(CommerceNotificationTemplateResponse::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceNotificationTemplateResponse getTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return CommerceNotificationTemplateResponse.from(findByUid(templateUid));
	}

	@Override
	@Transactional
	public CommerceNotificationTemplateResponse updateTemplate(
			String templateUid,
			CommerceNotificationTemplateCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceNotificationTemplate template = findByUid(templateUid);
		ValidatedTemplateInput input = validateCommand(command);
		template.setSubject(input.subject());
		template.setContent(input.content());
		template.setActive(input.active());
		return CommerceNotificationTemplateResponse.from(templateRepository.save(template));
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceNotificationTemplatePreviewResponse previewTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceNotificationTemplate template = findByUid(templateUid);
		Map<String, String> variables = sampleVariables();
		return new CommerceNotificationTemplatePreviewResponse(
				template.getUid(),
				templateVariableRenderer.render(template.getSubject(), variables),
				templateVariableRenderer.render(template.getContent(), variables));
	}

	private CommerceNotificationTemplate findByUid(String templateUid) {
		return templateRepository.findByUid(requiredUid(templateUid))
				.orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
	}

	private ValidatedTemplateInput validateCommand(CommerceNotificationTemplateCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("commerce.notification.template.payload.required");
		}
		String subject = requiredText(
				command.subject(),
				"commerce.notification.template.subject.required",
				SUBJECT_MAX_LENGTH);
		String content = requiredText(
				command.content(),
				"commerce.notification.template.content.required",
				CONTENT_MAX_LENGTH);
		if (command.active() == null) {
			throw new IllegalArgumentException("commerce.notification.template.active.required");
		}
		return new ValidatedTemplateInput(subject, content, command.active());
	}

	private String requiredUid(String uid) {
		if (uid == null || uid.isBlank()) {
			throw new IllegalArgumentException("commerce.notification.template.uid.required");
		}
		return uid.trim();
	}

	private String requiredText(String value, String messageKey, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(messageKey);
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new IllegalArgumentException(messageKey);
		}
		return trimmed;
	}

	private String normalizeOptionalLanguage(String language) {
		if (language == null || language.isBlank()) {
			return null;
		}
		return language.trim().split("[-_]")[0].toUpperCase(Locale.ROOT);
	}

	private Map<String, String> sampleVariables() {
		Map<String, String> variables = new LinkedHashMap<>();
		variables.put("customerName", "Jane Doe");
		variables.put("orderNumber", "ORD-20260621-000001");
		variables.put("orderTotal", money(BigDecimal.valueOf(1250)));
		variables.put("currencyIso", "TRY");
		variables.put("orderStatus", "PAID");
		variables.put("carrierName", "Demo Cargo");
		variables.put("trackingNumber", "TRK-20260621");
		variables.put("trackingUrl", "https://tracking.example/TRK-20260621");
		variables.put("orderUrl", "https://demo.example.com/tr/account/orders/order-uid");
		variables.put("accountOrdersUrl", "https://demo.example.com/tr/account/orders");
		variables.put("requestType", "CANCELLATION");
		variables.put("requestReason", "Changed mind");
		variables.put("requestStatus", "APPROVED");
		variables.put("decisionNote", "Refund approved");
		return variables;
	}

	private String money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP)
				.toPlainString();
	}

	private record ValidatedTemplateInput(
			String subject,
			String content,
			Boolean active) {
	}
}
