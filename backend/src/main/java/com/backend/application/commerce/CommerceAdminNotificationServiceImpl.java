package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.CommerceNotificationTemplate;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.UserRepository;
import com.backend.shared.common.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class CommerceAdminNotificationServiceImpl implements CommerceAdminNotificationService {

	private static final String ADMIN_EMAIL_ENABLED_KEY = "commerce.notifications.admin.email.enabled";
	private static final String ADMIN_EMAIL_EVENT_ENABLED_PREFIX = "commerce.notifications.admin.email.";
	private static final String EVENT_ENABLED_SUFFIX = ".enabled";
	private static final String LANGUAGE_TR = "TR";
	private static final String LANGUAGE_EN = "EN";
	private static final String AGGREGATE_ORDER = "ORDER";
	private static final String AGGREGATE_ORDER_REQUEST = "ORDER_REQUEST";
	private static final String AGGREGATE_PAYMENT_ATTEMPT = "PAYMENT_ATTEMPT";
	private static final String OPERATION_PAYMENT_INITIALIZE = "PAYMENT_INITIALIZE";
	private static final String OPERATION_REFUND = "REFUND";
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final CommerceNotificationTemplateRepository templateRepository;
	private final CommerceNotificationOutboxRepository outboxRepository;
	private final UserRepository userRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final FrontendConfigPort frontendConfig;
	private final TemplateVariableRenderer templateVariableRenderer;
	private final CommerceNotificationDispatchService dispatchService;

	@Override
	public void notifyOrderCreated(CommerceOrder order) {
		if (order == null) {
			return;
		}
		queue(
				CommerceNotificationEventType.ADMIN_ORDER_CREATED,
				AGGREGATE_ORDER,
				order.getUid(),
				order,
				null,
				null,
				null,
				null,
				null);
	}

	@Override
	public void notifyOrderRequestCreated(CommerceOrderResolutionRequest request) {
		if (request == null || request.getOrder() == null) {
			return;
		}
		queue(
				CommerceNotificationEventType.ADMIN_ORDER_REQUEST_CREATED,
				AGGREGATE_ORDER_REQUEST,
				request.getUid(),
				request.getOrder(),
				request,
				null,
				null,
				null,
				null);
	}

	@Override
	public void notifyPaymentOperationFailed(CommercePaymentAttempt attempt, String operationType) {
		if (attempt == null) {
			return;
		}
		queue(
				CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
				AGGREGATE_PAYMENT_ATTEMPT,
				attempt.getUid(),
				null,
				null,
				attempt,
				nonBlankOrDefault(operationType, OPERATION_PAYMENT_INITIALIZE),
				attempt.getFailureCode(),
				attempt.getFailureMessageKey());
	}

	@Override
	public void notifyRefundOperationFailed(CommerceOrderResolutionRequest request) {
		if (request == null || request.getOrder() == null) {
			return;
		}
		queue(
				CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
				AGGREGATE_ORDER_REQUEST,
				request.getUid(),
				request.getOrder(),
				request,
				null,
				OPERATION_REFUND,
				request.getRefundFailureCode(),
				request.getRefundFailureMessageKey());
	}

	private void queue(
			CommerceNotificationEventType eventType,
			String aggregateType,
			String aggregateUid,
			CommerceOrder order,
			CommerceOrderResolutionRequest request,
			CommercePaymentAttempt attempt,
			String operationType,
			String failureCode,
			String failureMessageKey) {
		try {
			commerceModuleAccessGuard.assertEnabledForCurrentTenant();
			if (!notificationEnabled(eventType)) {
				return;
			}
			List<String> recipients = adminRecipients();
			if (recipients.isEmpty()) {
				return;
			}
			String language = defaultLanguage();
			Optional<CommerceNotificationTemplate> template = activeTemplate(eventType, language);
			if (template.isEmpty()) {
				log.warn("Commerce admin notification template missing event={} language={}", eventType, language);
				return;
			}

			String deliveryLanguage = template.get().getLanguage();
			Map<String, String> variables = variables(
					order,
					request,
					attempt,
					operationType,
					failureCode,
					failureMessageKey,
					deliveryLanguage);
			for (String recipient : recipients) {
				CommerceNotificationOutbox outbox = new CommerceNotificationOutbox();
				outbox.setEventType(eventType);
				outbox.setChannel(CommerceNotificationChannel.EMAIL);
				outbox.setAggregateType(aggregateType);
				outbox.setAggregateUid(nonBlankOrDefault(aggregateUid, "UNKNOWN"));
				outbox.setRecipientEmail(recipient);
				outbox.setLanguage(deliveryLanguage);
				outbox.setSubject(templateVariableRenderer.render(template.get().getSubject(), variables));
				outbox.setContent(templateVariableRenderer.render(template.get().getContent(), variables));
				outbox.setStatus(CommerceNotificationStatus.PENDING);
				CommerceNotificationOutbox saved = outboxRepository.save(outbox);
				dispatchService.dispatchAfterCommit(saved.getId());
			}
		} catch (RuntimeException ex) {
			log.warn(
					"Commerce admin notification queue failed event={} aggregateType={} aggregateUid={} reason={}",
					eventType,
					aggregateType,
					aggregateUid,
					LogSanitizer.sanitizeForLog(ex.getMessage()));
		}
	}

	private boolean notificationEnabled(CommerceNotificationEventType eventType) {
		boolean globalEnabled = configPropertyService.getBoolean(
				currentTenantId(),
				tenantContext.getTenantDbName(),
				ADMIN_EMAIL_ENABLED_KEY,
				false);
		return configPropertyService.getBoolean(
				currentTenantId(),
				tenantContext.getTenantDbName(),
				ADMIN_EMAIL_EVENT_ENABLED_PREFIX + eventType.name().toLowerCase(Locale.ROOT) + EVENT_ENABLED_SUFFIX,
				globalEnabled);
	}

	private List<String> adminRecipients() {
		return userRepository.findByRole(UserRole.TENANT_ADMIN).stream()
				.filter(user -> Boolean.TRUE.equals(user.getIsActive()))
				.map(User::getEmail)
				.filter(StringUtils::hasText)
				.map(email -> email.trim().toLowerCase(Locale.ROOT))
				.filter(this::validEmail)
				.distinct()
				.toList();
	}

	private boolean validEmail(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}

	private Optional<CommerceNotificationTemplate> activeTemplate(
			CommerceNotificationEventType eventType,
			String language) {
		Optional<CommerceNotificationTemplate> exactTemplate = templateRepository.findExact(
				eventType,
				CommerceNotificationChannel.EMAIL,
				language);
		if (exactTemplate.isPresent()) {
			return Boolean.TRUE.equals(exactTemplate.get().getActive())
					? exactTemplate
					: Optional.empty();
		}
		if (LANGUAGE_EN.equals(language)) {
			return Optional.empty();
		}
		return templateRepository.findActive(eventType, CommerceNotificationChannel.EMAIL, LANGUAGE_EN);
	}

	private Map<String, String> variables(
			CommerceOrder order,
			CommerceOrderResolutionRequest request,
			CommercePaymentAttempt attempt,
			String operationType,
			String failureCode,
			String failureMessageKey,
			String language) {
		CommerceOrder resolvedOrder = order;
		CommerceCustomer customer = customer(resolvedOrder, request, attempt);
		Map<String, String> variables = new LinkedHashMap<>();
		variables.put("orderNumber", resolvedOrder == null ? "" : value(resolvedOrder.getOrderNumber()));
		variables.put("orderTotal", resolvedOrder == null ? "" : money(resolvedOrder.getTotal()));
		variables.put("currencyIso", resolvedOrder == null ? "" : value(resolvedOrder.getCurrencyIso()));
		variables.put("orderStatus", resolvedOrder == null || resolvedOrder.getStatus() == null ? "" : resolvedOrder.getStatus().name());
		variables.put("orderUid", resolvedOrder == null ? "" : value(resolvedOrder.getUid()));
		variables.put("customerName", customerName(customer));
		variables.put("customerEmail", customer == null ? "" : value(customer.getEmail()));
		variables.put("requestType", request == null || request.getType() == null ? "" : request.getType().name());
		variables.put("requestReason", request == null ? "" : value(request.getReason()));
		variables.put("requestUid", request == null ? "" : value(request.getUid()));
		variables.put("operationType", value(operationType));
		variables.put("failureCode", value(failureCode));
		variables.put("failureMessageKey", value(failureMessageKey));
		variables.put("attemptUid", attempt == null ? "" : value(attempt.getUid()));
		variables.put("adminOrderUrl", adminOrderUrl(resolvedOrder, language));
		variables.put("adminOrderRequestUrl", adminOrderRequestUrl(request, language));
		variables.put("adminPaymentAttemptsUrl", adminUrl("/commerce/payment-attempts", language));
		return variables;
	}

	private CommerceCustomer customer(
			CommerceOrder order,
			CommerceOrderResolutionRequest request,
			CommercePaymentAttempt attempt) {
		if (order != null) {
			return order.getCustomer();
		}
		if (request != null) {
			return request.getCustomer();
		}
		return attempt == null ? null : attempt.getCustomer();
	}

	private String adminOrderUrl(CommerceOrder order, String language) {
		if (order == null || !StringUtils.hasText(order.getUid())) {
			return "";
		}
		return adminUrl("/commerce/orders/" + order.getUid(), language);
	}

	private String adminOrderRequestUrl(CommerceOrderResolutionRequest request, String language) {
		if (request == null || !StringUtils.hasText(request.getUid())) {
			return "";
		}
		return adminUrl("/commerce/order-requests/" + request.getUid(), language);
	}

	private String adminUrl(String path, String language) {
		try {
			String base = frontendConfig.getBaseUrl();
			if (!StringUtils.hasText(base)) {
				return "";
			}
			String resolved = base.trim();
			if (resolved.contains("%s")) {
				resolved = String.format(resolved, Objects.toString(tenantContext.getSubdomain(), ""));
			}
			while (resolved.endsWith("/")) {
				resolved = resolved.substring(0, resolved.length() - 1);
			}
			return resolved + "/" + normalizeLanguage(language, defaultLanguage()).toLowerCase(Locale.ROOT) + path;
		} catch (RuntimeException ex) {
			return "";
		}
	}

	private String defaultLanguage() {
		Language language = tenantContext.getDefaultLanguage();
		return language == Language.TR ? LANGUAGE_TR : LANGUAGE_EN;
	}

	private String normalizeLanguage(String language, String fallback) {
		if (!StringUtils.hasText(language)) {
			return fallback;
		}
		return language.trim().toUpperCase(Locale.ROOT).startsWith(LANGUAGE_TR) ? LANGUAGE_TR : LANGUAGE_EN;
	}

	private String customerName(CommerceCustomer customer) {
		if (customer == null) {
			return "";
		}
		StringBuilder name = new StringBuilder();
		appendNamePart(name, customer.getFirstName());
		appendNamePart(name, customer.getLastName());
		if (!name.isEmpty()) {
			return name.toString();
		}
		return value(customer.getEmail());
	}

	private void appendNamePart(StringBuilder name, String value) {
		if (!StringUtils.hasText(value)) {
			return;
		}
		if (!name.isEmpty()) {
			name.append(' ');
		}
		name.append(value.trim());
	}

	private String money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP)
				.toPlainString();
	}

	private String value(String value) {
		return StringUtils.hasText(value) ? value.trim() : "";
	}

	private String nonBlankOrDefault(String value, String defaultValue) {
		return StringUtils.hasText(value) ? value.trim() : defaultValue;
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}
}
