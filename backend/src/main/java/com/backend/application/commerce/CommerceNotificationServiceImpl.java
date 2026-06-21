package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.backend.application.dto.email.EmailResult;
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
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.domain.enums.Language;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.shared.common.LogSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class CommerceNotificationServiceImpl implements CommerceNotificationService {

	private static final String EMAIL_ENABLED_KEY = "commerce.notifications.email.enabled";
	private static final String EVENT_ENABLED_PREFIX = "commerce.notifications.email.";
	private static final String EVENT_ENABLED_SUFFIX = ".enabled";
	private static final String LANGUAGE_TR = "TR";
	private static final String LANGUAGE_EN = "EN";
	private static final String AGGREGATE_ORDER = "ORDER";
	private static final String AGGREGATE_ORDER_REQUEST = "ORDER_REQUEST";

	private final CommerceNotificationTemplateRepository templateRepository;
	private final CommerceNotificationOutboxRepository outboxRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final FrontendConfigPort frontendConfig;
	private final TemplateVariableRenderer templateVariableRenderer;
	private final MailSenderPort mailSender;
	private final ObjectMapper objectMapper;

	@Qualifier("tenantTransactionManager")
	private final PlatformTransactionManager tenantTransactionManager;

	@Override
	public void notifyOrderPaid(CommerceOrder order) {
		notifyOrderEvent(CommerceNotificationEventType.ORDER_PAID, order, paidLanguage(order));
	}

	@Override
	public void notifyOrderShipped(CommerceOrder order) {
		notifyOrderEvent(CommerceNotificationEventType.ORDER_SHIPPED, order, orderLanguage(order));
	}

	@Override
	public void notifyOrderRequestCreated(CommerceOrderResolutionRequest request) {
		if (request == null || request.getOrder() == null) {
			return;
		}
		notifyRequestEvent(
				CommerceNotificationEventType.ORDER_REQUEST_CREATED,
				request,
				normalizeLanguage(LocaleContextHolder.getLocale().getLanguage(), orderLanguage(request.getOrder())));
	}

	@Override
	public void notifyOrderRequestDecided(CommerceOrderResolutionRequest request) {
		if (request == null || request.getOrder() == null) {
			return;
		}
		if (request.getStatus() == CommerceOrderResolutionRequestStatus.APPROVED) {
			notifyRequestEvent(CommerceNotificationEventType.ORDER_REQUEST_APPROVED, request, orderLanguage(request.getOrder()));
			return;
		}
		if (request.getStatus() == CommerceOrderResolutionRequestStatus.REJECTED) {
			notifyRequestEvent(CommerceNotificationEventType.ORDER_REQUEST_REJECTED, request, orderLanguage(request.getOrder()));
		}
	}

	private void notifyOrderEvent(CommerceNotificationEventType eventType, CommerceOrder order, String language) {
		if (order == null) {
			return;
		}
		queue(eventType, AGGREGATE_ORDER, order.getUid(), order, null, language);
	}

	private void notifyRequestEvent(
			CommerceNotificationEventType eventType,
			CommerceOrderResolutionRequest request,
			String language) {
		if (request == null || request.getOrder() == null) {
			return;
		}
		queue(eventType, AGGREGATE_ORDER_REQUEST, request.getUid(), request.getOrder(), request, language);
	}

	private void queue(
			CommerceNotificationEventType eventType,
			String aggregateType,
			String aggregateUid,
			CommerceOrder order,
			CommerceOrderResolutionRequest request,
			String language) {
		try {
			commerceModuleAccessGuard.assertEnabledForCurrentTenant();
			if (!notificationEnabled(eventType)) {
				return;
			}
			CommerceCustomer customer = order.getCustomer();
			if (customer == null || !StringUtils.hasText(customer.getEmail())) {
				return;
			}
			String normalizedLanguage = normalizeLanguage(language, defaultLanguage());
			Optional<CommerceNotificationTemplate> template = activeTemplate(eventType, normalizedLanguage);
			if (template.isEmpty()) {
				log.warn("Commerce notification template missing event={} language={}", eventType, normalizedLanguage);
				return;
			}

			String deliveryLanguage = template.get().getLanguage();
			Map<String, String> variables = variables(order, request, deliveryLanguage);
			CommerceNotificationOutbox outbox = new CommerceNotificationOutbox();
			outbox.setEventType(eventType);
			outbox.setChannel(CommerceNotificationChannel.EMAIL);
			outbox.setAggregateType(aggregateType);
			outbox.setAggregateUid(aggregateUid);
			outbox.setRecipientEmail(customer.getEmail().trim().toLowerCase(Locale.ROOT));
			outbox.setLanguage(deliveryLanguage);
			outbox.setSubject(templateVariableRenderer.render(template.get().getSubject(), variables));
			outbox.setContent(templateVariableRenderer.render(template.get().getContent(), variables));
			outbox.setStatus(CommerceNotificationStatus.PENDING);
			CommerceNotificationOutbox saved = outboxRepository.save(outbox);
			dispatchAfterCommit(saved.getId());
		} catch (RuntimeException ex) {
			log.warn(
					"Commerce notification queue failed event={} aggregateType={} aggregateUid={} reason={}",
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
				EMAIL_ENABLED_KEY,
				false);
		return configPropertyService.getBoolean(
				currentTenantId(),
				tenantContext.getTenantDbName(),
				EVENT_ENABLED_PREFIX + eventType.name().toLowerCase(Locale.ROOT) + EVENT_ENABLED_SUFFIX,
				globalEnabled);
	}

	private Optional<CommerceNotificationTemplate> activeTemplate(
			CommerceNotificationEventType eventType,
			String language) {
		Optional<CommerceNotificationTemplate> template = templateRepository.findActive(
				eventType,
				CommerceNotificationChannel.EMAIL,
				language);
		if (template.isPresent() || LANGUAGE_EN.equals(language)) {
			return template;
		}
		return templateRepository.findActive(eventType, CommerceNotificationChannel.EMAIL, LANGUAGE_EN);
	}

	private Map<String, String> variables(
			CommerceOrder order,
			CommerceOrderResolutionRequest request,
			String language) {
		Map<String, String> variables = new LinkedHashMap<>();
		CommerceCustomer customer = order.getCustomer();
		variables.put("customerName", customerName(customer));
		variables.put("orderNumber", value(order.getOrderNumber()));
		variables.put("orderTotal", money(order.getTotal()));
		variables.put("currencyIso", value(order.getCurrencyIso()));
		variables.put("orderStatus", order.getStatus() == null ? "" : order.getStatus().name());
		variables.put("carrierName", value(order.getShippingCarrierName()));
		variables.put("trackingNumber", value(order.getShippingTrackingNumber()));
		variables.put("trackingUrl", value(order.getShippingTrackingUrl()));
		variables.put("orderUrl", orderUrl(order, language));
		variables.put("accountOrdersUrl", accountOrdersUrl(language));
		if (request == null) {
			variables.put("requestType", "");
			variables.put("requestReason", "");
			variables.put("requestStatus", "");
			variables.put("decisionNote", "");
			return variables;
		}
		variables.put("requestType", request.getType() == null ? "" : request.getType().name());
		variables.put("requestReason", value(request.getReason()));
		variables.put("requestStatus", request.getStatus() == null ? "" : request.getStatus().name());
		variables.put("decisionNote", value(request.getDecisionNote()));
		return variables;
	}

	private void dispatchAfterCommit(Long outboxId) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						dispatch(outboxId);
					} catch (RuntimeException ex) {
						log.warn(
								"Commerce notification dispatch failed outboxId={} reason={}",
								outboxId,
								LogSanitizer.sanitizeForLog(ex.getMessage()));
					}
				}
			});
			return;
		}
		dispatch(outboxId);
	}

	private void dispatch(Long outboxId) {
		PendingEmail pendingEmail = findPendingEmail(outboxId);
		if (pendingEmail == null) {
			return;
		}
		EmailResult result;
		try {
			result = mailSender.send(pendingEmail.recipientEmail(), pendingEmail.subject(), pendingEmail.content());
		} catch (RuntimeException ex) {
			result = EmailResult.failure(LogSanitizer.sanitizeForLog(ex.getMessage()));
		}
		updateOutboxResult(outboxId, result);
	}

	private PendingEmail findPendingEmail(Long outboxId) {
		return newRequiresNewTransactionTemplate().execute(status -> {
			CommerceNotificationOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
			if (outbox == null || outbox.getStatus() != CommerceNotificationStatus.PENDING) {
				return null;
			}
			return new PendingEmail(outbox.getRecipientEmail(), outbox.getSubject(), outbox.getContent());
		});
	}

	private void updateOutboxResult(Long outboxId, EmailResult result) {
		newRequiresNewTransactionTemplate().execute(status -> {
			CommerceNotificationOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
			if (outbox == null || outbox.getStatus() != CommerceNotificationStatus.PENDING) {
				return null;
			}
			if (result.isSuccess()) {
				outbox.setStatus(CommerceNotificationStatus.SENT);
				outbox.setProviderMessageId(result.getMessageId());
				outbox.setErrorMessage(null);
				outbox.setSentAt(LocalDateTime.now());
			} else {
				outbox.setStatus(CommerceNotificationStatus.FAILED);
				outbox.setProviderMessageId(null);
				outbox.setErrorMessage(LogSanitizer.sanitizeForLog(result.getErrorMessage()));
			}
			outboxRepository.save(outbox);
			return null;
		});
	}

	private TransactionTemplate newRequiresNewTransactionTemplate() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return transactionTemplate;
	}

	private record PendingEmail(String recipientEmail, String subject, String content) {
	}

	private String paidLanguage(CommerceOrder order) {
		return languageFromLegalSnapshot(order).orElse(defaultLanguage());
	}

	private String orderLanguage(CommerceOrder order) {
		return languageFromLegalSnapshot(order).orElse(defaultLanguage());
	}

	private Optional<String> languageFromLegalSnapshot(CommerceOrder order) {
		if (order == null || !StringUtils.hasText(order.getLegalSnapshotJson())) {
			return Optional.empty();
		}
		try {
			JsonNode language = objectMapper.readTree(order.getLegalSnapshotJson()).path("language");
			if (language.isMissingNode() || !StringUtils.hasText(language.asText())) {
				return Optional.empty();
			}
			return Optional.of(normalizeLanguage(language.asText(), defaultLanguage()));
		} catch (Exception ex) {
			return Optional.empty();
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

	private String orderUrl(CommerceOrder order, String language) {
		if (order == null || !StringUtils.hasText(order.getUid())) {
			return accountOrdersUrl(language);
		}
		String base = accountOrdersUrl(language);
		return StringUtils.hasText(base) ? base + "/" + order.getUid() : "";
	}

	private String accountOrdersUrl(String language) {
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
			return resolved + "/" + normalizeLanguage(language, defaultLanguage()).toLowerCase(Locale.ROOT) + "/account/orders";
		} catch (RuntimeException ex) {
			return "";
		}
	}

	private String money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP)
				.toPlainString();
	}

	private String value(String value) {
		return StringUtils.hasText(value) ? value.trim() : "";
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}
}
