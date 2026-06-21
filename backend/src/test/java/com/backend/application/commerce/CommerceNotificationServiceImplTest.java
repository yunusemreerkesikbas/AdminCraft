package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.dto.sms.SmsResult;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.CommerceNotificationTemplate;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderLegalSnapshotStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.domain.enums.Language;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.SmsSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommerceNotificationServiceImplTest extends BaseServiceTest {

	@Mock private CommerceNotificationTemplateRepository templateRepository;
	@Mock private CommerceNotificationOutboxRepository outboxRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;
	@Mock private FrontendConfigPort frontendConfig;
	@Mock private MailSenderPort mailSender;
	@Mock private SmsSenderPort smsSender;
	@Mock private PlatformTransactionManager transactionManager;

	private final AtomicReference<CommerceNotificationOutbox> savedOutbox = new AtomicReference<>();
	private final AtomicBoolean tenantTransactionActive = new AtomicBoolean();
	private CommerceNotificationServiceImpl service;
	private CommerceNotificationProperties properties;

	@BeforeEach
	void setUp() {
		properties = new CommerceNotificationProperties();
		CommerceNotificationDispatchService dispatchService = new CommerceNotificationDispatchService(
				outboxRepository,
				mailSender,
				smsSender,
				properties,
				transactionManager);
		service = new CommerceNotificationServiceImpl(
				templateRepository,
				outboxRepository,
				commerceModuleAccessGuard,
				configPropertyService,
				tenantContext,
				frontendConfig,
				new TemplateVariableRenderer(),
				new ObjectMapper(),
				dispatchService);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
		lenient().when(tenantContext.getSubdomain()).thenReturn("demo");
		lenient().when(tenantContext.getDefaultLanguage()).thenReturn(Language.EN);
		lenient().when(frontendConfig.getBaseUrl()).thenReturn("https://%s.example.com");
		lenient().when(configPropertyService.getBoolean(anyLong(), anyString(), anyString(), anyBoolean()))
				.thenAnswer(invocation -> invocation.getArgument(3));
		lenient().when(transactionManager.getTransaction(any())).thenAnswer(invocation -> {
			tenantTransactionActive.set(true);
			return new SimpleTransactionStatus();
		});
		lenient().doAnswer(invocation -> {
			tenantTransactionActive.set(false);
			return null;
		}).when(transactionManager).commit(any());
		lenient().doAnswer(invocation -> {
			tenantTransactionActive.set(false);
			return null;
		}).when(transactionManager).rollback(any());
		lenient().when(outboxRepository.save(any())).thenAnswer(invocation -> {
			CommerceNotificationOutbox outbox = invocation.getArgument(0);
			if (outbox.getId() == null) {
				outbox.setId(99L);
			}
			savedOutbox.set(outbox);
			return outbox;
		});
		lenient().when(outboxRepository.findByIdForUpdate(99L)).thenAnswer(invocation -> Optional.of(savedOutbox.get()));
	}

	@Test
	void notifyOrderPaid_ShouldSkip_WhenEmailNotificationsDisabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(false);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.email.order_paid.enabled",
				false))
				.thenReturn(false);

		service.notifyOrderPaid(orderWithLegalLanguage("TR"));

		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderPaid_ShouldUseEnglishTemplateFallbackAndMarkSent() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.email.order_paid.enabled",
				true))
				.thenReturn(true);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"TR"))
				.thenReturn(Optional.empty());
		when(templateRepository.findActive(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"EN"))
				.thenReturn(Optional.of(template("EN")));
		when(mailSender.send("jane@example.com", "Order ORD-1", "Hello Jane Doe: 200.00 TRY https://demo.example.com/en/account/orders/order-uid"))
				.thenAnswer(invocation -> {
					assertThat(tenantTransactionActive).isTrue();
					return EmailResult.success("message-1");
				});

		service.notifyOrderPaid(orderWithLegalLanguage("TR"));

		CommerceNotificationOutbox outbox = savedOutbox.get();
		assertThat(outbox.getLanguage()).isEqualTo("EN");
		assertThat(outbox.getSubject()).isEqualTo("Order ORD-1");
		assertThat(outbox.getContent()).isEqualTo("Hello Jane Doe: 200.00 TRY https://demo.example.com/en/account/orders/order-uid");
		assertThat(outbox.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
		assertThat(outbox.getChannel()).isEqualTo(CommerceNotificationChannel.EMAIL);
		assertThat(outbox.getProviderMessageId()).isEqualTo("message-1");
		assertThat(outbox.getSentAt()).isNotNull();
		ArgumentCaptor<TransactionDefinition> definitionCaptor = ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(transactionManager).getTransaction(definitionCaptor.capture());
		assertThat(definitionCaptor.getAllValues())
				.allMatch(definition -> definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	@Test
	void notifyOrderPaid_ShouldQueueSms_WhenSmsEnabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(false);
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.sms.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.sms.order_paid.enabled",
				true))
				.thenReturn(true);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.SMS,
				"TR"))
				.thenReturn(Optional.of(smsTemplate("TR")));
		when(smsSender.send("905551112233", "SMS ORD-1 200.00 TRY")).thenReturn(SmsResult.success("sms-1"));

		service.notifyOrderPaid(orderWithLegalLanguage("TR"));

		CommerceNotificationOutbox outbox = savedOutbox.get();
		assertThat(outbox.getChannel()).isEqualTo(CommerceNotificationChannel.SMS);
		assertThat(outbox.getRecipientEmail()).isNull();
		assertThat(outbox.getRecipientPhone()).isEqualTo("905551112233");
		assertThat(outbox.getContent()).isEqualTo("SMS ORD-1 200.00 TRY");
		assertThat(outbox.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
		assertThat(outbox.getProviderMessageId()).isEqualTo("sms-1");
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void notifyOrderPaid_ShouldSkipSms_WhenEventOverrideDisabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(false);
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.sms.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.sms.order_paid.enabled",
				true))
				.thenReturn(false);

		service.notifyOrderPaid(orderWithLegalLanguage("TR"));

		verify(outboxRepository, never()).save(any());
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderPaid_ShouldSkipSms_WhenCustomerPhoneMissing() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(false);
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.sms.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.sms.order_paid.enabled",
				true))
				.thenReturn(true);
		CommerceOrder order = orderWithLegalLanguage("TR");
		order.getCustomer().setPhone("123");

		service.notifyOrderPaid(order);

		verify(templateRepository, never()).findExact(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.SMS,
				"TR");
		verify(outboxRepository, never()).save(any());
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderRequestCreated_ShouldNotQueueSms() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(false);
		CommerceOrderResolutionRequest request = new CommerceOrderResolutionRequest();
		request.setUid("request-uid");
		request.setOrder(orderWithLegalLanguage("TR"));

		service.notifyOrderRequestCreated(request);

		verify(outboxRepository, never()).save(any());
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderPaid_ShouldMarkFailed_WhenMailSenderFails() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.email.order_paid.enabled",
				true))
				.thenReturn(true);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"EN"))
				.thenReturn(Optional.of(template("EN")));
		when(mailSender.send(any(), any(), any())).thenReturn(EmailResult.failure("provider down"));

		service.notifyOrderPaid(orderWithLegalLanguage(null));

		CommerceNotificationOutbox outbox = savedOutbox.get();
		assertThat(outbox.getStatus()).isEqualTo(CommerceNotificationStatus.FAILED);
		assertThat(outbox.getAttemptCount()).isEqualTo(1);
		assertThat(outbox.getLastAttemptedAt()).isNotNull();
		assertThat(outbox.getNextRetryAt()).isNotNull();
		assertThat(outbox.getErrorMessage()).isEqualTo("provider down");
		assertThat(outbox.getProviderMessageId()).isNull();
	}

	@Test
	void notifyOrderPaid_ShouldSkip_WhenExactLanguageTemplateIsInactive() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.notifications.email.enabled", false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.email.order_paid.enabled",
				true))
				.thenReturn(true);
		CommerceNotificationTemplate inactive = template("TR");
		inactive.setActive(false);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"TR"))
				.thenReturn(Optional.of(inactive));

		service.notifyOrderPaid(orderWithLegalLanguage("TR"));

		verify(templateRepository, never()).findActive(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"EN");
		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderRequestCreated_ShouldSkip_WhenRequestOrOrderMissing() {
		Assertions.assertDoesNotThrow(() -> service.notifyOrderRequestCreated(null));
		Assertions.assertDoesNotThrow(() -> service.notifyOrderRequestCreated(new CommerceOrderResolutionRequest()));

		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void notifyOrderRequestDecided_ShouldSkip_WhenRequestOrOrderMissing() {
		Assertions.assertDoesNotThrow(() -> service.notifyOrderRequestDecided(null));
		Assertions.assertDoesNotThrow(() -> service.notifyOrderRequestDecided(new CommerceOrderResolutionRequest()));

		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
	}

	private CommerceNotificationTemplate template(String language) {
		CommerceNotificationTemplate template = new CommerceNotificationTemplate();
		template.setTemplateKey(CommerceNotificationEventType.ORDER_PAID);
		template.setChannel(CommerceNotificationChannel.EMAIL);
		template.setLanguage(language);
		template.setSubject("Order {{orderNumber}}");
		template.setContent("Hello {{customerName}}: {{orderTotal}} {{currencyIso}} {{orderUrl}}");
		template.setActive(true);
		return template;
	}

	private CommerceNotificationTemplate smsTemplate(String language) {
		CommerceNotificationTemplate template = new CommerceNotificationTemplate();
		template.setTemplateKey(CommerceNotificationEventType.ORDER_PAID);
		template.setChannel(CommerceNotificationChannel.SMS);
		template.setLanguage(language);
		template.setSubject("ORDER_PAID");
		template.setContent("SMS {{orderNumber}} {{orderTotal}} {{currencyIso}}");
		template.setActive(true);
		return template;
	}

	private CommerceOrder orderWithLegalLanguage(String language) {
		CommerceOrder order = new CommerceOrder();
		order.setId(1L);
		order.setUid("order-uid");
		order.setOrderNumber("ORD-1");
		order.setCustomer(customer());
		order.setStatus(CommerceOrderStatus.PAID);
		order.setCurrencyIso("TRY");
		order.setTotal(BigDecimal.valueOf(200).setScale(2));
		order.setLegalSnapshotStatus(language == null
				? CommerceOrderLegalSnapshotStatus.NOT_CAPTURED
				: CommerceOrderLegalSnapshotStatus.CAPTURED);
		if (language != null) {
			order.setLegalSnapshotJson("{\"language\":\"" + language + "\"}");
		}
		return order;
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("jane@example.com");
		customer.setFirstName("Jane");
		customer.setLastName("Doe");
		customer.setPhone("+90 555 111 22 33");
		return customer;
	}
}
