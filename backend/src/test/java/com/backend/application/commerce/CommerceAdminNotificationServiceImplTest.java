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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

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
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.SmsSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.UserRepository;
import com.backend.testutil.BaseServiceTest;

class CommerceAdminNotificationServiceImplTest extends BaseServiceTest {

	@Mock private CommerceNotificationTemplateRepository templateRepository;
	@Mock private CommerceNotificationOutboxRepository outboxRepository;
	@Mock private UserRepository userRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;
	@Mock private FrontendConfigPort frontendConfig;
	@Mock private MailSenderPort mailSender;
	@Mock private SmsSenderPort smsSender;
	@Mock private PlatformTransactionManager transactionManager;

	private final AtomicLong outboxId = new AtomicLong(100);
	private final List<CommerceNotificationOutbox> savedOutboxes = new ArrayList<>();
	private final AtomicBoolean tenantTransactionActive = new AtomicBoolean();
	private CommerceAdminNotificationServiceImpl service;
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
		service = new CommerceAdminNotificationServiceImpl(
				templateRepository,
				outboxRepository,
				userRepository,
				commerceModuleAccessGuard,
				configPropertyService,
				tenantContext,
				frontendConfig,
				new TemplateVariableRenderer(),
				dispatchService);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
		lenient().when(tenantContext.getSubdomain()).thenReturn("demo");
		lenient().when(tenantContext.getDefaultLanguage()).thenReturn(Language.TR);
		lenient().when(frontendConfig.getBaseUrl()).thenReturn("https://%s.admin.example.com");
		lenient().when(configPropertyService.getBoolean(anyLong(), anyString(), anyString(), anyBoolean()))
				.thenAnswer(invocation -> invocation.getArgument(3));
		lenient().when(userRepository.findByRole(UserRole.TENANT_ADMIN))
				.thenReturn(List.of(activeAdmin("owner@example.com")));
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
				outbox.setId(outboxId.incrementAndGet());
			}
			savedOutboxes.removeIf(existing -> existing.getId().equals(outbox.getId()));
			savedOutboxes.add(outbox);
			return outbox;
		});
		lenient().when(outboxRepository.findByIdForUpdate(anyLong())).thenAnswer(invocation -> {
			Long id = invocation.getArgument(0);
			return savedOutboxes.stream().filter(outbox -> outbox.getId().equals(id)).findFirst();
		});
	}

	@Test
	void notifyOrderCreated_ShouldSkip_WhenAdminEmailNotificationsDisabled() {
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.enabled",
				false))
				.thenReturn(false);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.admin_order_created.enabled",
				false))
				.thenReturn(false);

		service.notifyOrderCreated(order());

		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void notifyOrderCreated_ShouldQueueOneEmailPerActiveTenantAdmin() {
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.enabled",
				false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.admin_order_created.enabled",
				true))
				.thenReturn(true);
		when(userRepository.findByRole(UserRole.TENANT_ADMIN))
				.thenReturn(List.of(
						activeAdmin("OWNER@example.com"),
						activeAdmin("owner@example.com"),
						activeAdmin("invalid-email"),
						inactiveAdmin("inactive@example.com"),
						activeAdmin("ops@example.com")));
		when(templateRepository.findExact(
				CommerceNotificationEventType.ADMIN_ORDER_CREATED,
				CommerceNotificationChannel.EMAIL,
				"TR"))
				.thenReturn(Optional.of(template(
						CommerceNotificationEventType.ADMIN_ORDER_CREATED,
						"TR",
						"Yeni {{orderNumber}}",
						"{{customerName}} {{customerEmail}} {{adminOrderUrl}}")));
		when(mailSender.send(any(), any(), any())).thenAnswer(invocation -> {
			assertThat(tenantTransactionActive).isTrue();
			return EmailResult.success("message-" + invocation.getArgument(0));
		});

		service.notifyOrderCreated(order());

		assertThat(savedOutboxes).hasSize(2);
		assertThat(savedOutboxes)
				.extracting(CommerceNotificationOutbox::getRecipientEmail)
				.containsExactlyInAnyOrder("owner@example.com", "ops@example.com");
		assertThat(savedOutboxes)
				.allSatisfy(outbox -> {
					assertThat(outbox.getEventType()).isEqualTo(CommerceNotificationEventType.ADMIN_ORDER_CREATED);
					assertThat(outbox.getChannel()).isEqualTo(CommerceNotificationChannel.EMAIL);
					assertThat(outbox.getSubject()).isEqualTo("Yeni ORD-1");
					assertThat(outbox.getContent()).contains("Jane Doe jane@example.com https://demo.admin.example.com/tr/commerce/orders/order-uid");
					assertThat(outbox.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
				});
		verify(smsSender, never()).send(any(), any());
	}

	@Test
	void notifyOrderRequestCreated_ShouldSkip_WhenEventOverrideDisabled() {
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.enabled",
				false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.admin_order_request_created.enabled",
				true))
				.thenReturn(false);

		service.notifyOrderRequestCreated(request());

		verify(outboxRepository, never()).save(any());
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void notifyPaymentOperationFailed_ShouldQueuePaymentAttemptsLink_WhenOrderIsMissing() {
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.enabled",
				false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.admin_payment_operation_failed.enabled",
				true))
				.thenReturn(true);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
				CommerceNotificationChannel.EMAIL,
				"TR"))
				.thenReturn(Optional.of(template(
						CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
						"TR",
						"Hata {{operationType}}",
						"{{attemptUid}} {{failureCode}} {{adminPaymentAttemptsUrl}}")));
		when(mailSender.send(any(), any(), any())).thenReturn(EmailResult.success("message-1"));

		service.notifyPaymentOperationFailed(paymentAttempt(), "PAYMENT_RETRIEVE");

		assertThat(savedOutboxes).hasSize(1);
		CommerceNotificationOutbox outbox = savedOutboxes.getFirst();
		assertThat(outbox.getAggregateType()).isEqualTo("PAYMENT_ATTEMPT");
		assertThat(outbox.getAggregateUid()).isEqualTo("attempt-uid");
		assertThat(outbox.getContent())
				.contains("attempt-uid PROVIDER_RETRIEVE_FAILED https://demo.admin.example.com/tr/commerce/payment-attempts");
	}

	@Test
	void notifyRefundOperationFailed_ShouldQueuePaymentOperationFailureAlert() {
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.enabled",
				false))
				.thenReturn(true);
		when(configPropertyService.getBoolean(
				1L,
				"tenant_1",
				"commerce.notifications.admin.email.admin_payment_operation_failed.enabled",
				true))
				.thenReturn(true);
		when(templateRepository.findExact(
				CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
				CommerceNotificationChannel.EMAIL,
				"TR"))
				.thenReturn(Optional.of(template(
						CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED,
						"TR",
						"Hata {{operationType}}",
						"{{failureCode}} {{failureMessageKey}} {{requestUid}} {{adminOrderUrl}}")));
		when(mailSender.send(any(), any(), any())).thenReturn(EmailResult.success("message-1"));

		service.notifyRefundOperationFailed(request());

		assertThat(savedOutboxes).hasSize(1);
		CommerceNotificationOutbox outbox = savedOutboxes.getFirst();
		assertThat(outbox.getEventType()).isEqualTo(CommerceNotificationEventType.ADMIN_PAYMENT_OPERATION_FAILED);
		assertThat(outbox.getSubject()).isEqualTo("Hata REFUND");
		assertThat(outbox.getContent()).contains("REFUND_FAILED commerce.payment.refund.failed request-uid");
		assertThat(outbox.getAggregateType()).isEqualTo("ORDER_REQUEST");
		assertThat(outbox.getAggregateUid()).isEqualTo("request-uid");
	}

	private CommerceNotificationTemplate template(
			CommerceNotificationEventType eventType,
			String language,
			String subject,
			String content) {
		CommerceNotificationTemplate template = new CommerceNotificationTemplate();
		template.setTemplateKey(eventType);
		template.setChannel(CommerceNotificationChannel.EMAIL);
		template.setLanguage(language);
		template.setSubject(subject);
		template.setContent(content);
		template.setActive(true);
		return template;
	}

	private CommerceOrderResolutionRequest request() {
		CommerceOrderResolutionRequest request = new CommerceOrderResolutionRequest();
		request.setId(50L);
		request.setUid("request-uid");
		request.setOrder(order());
		request.setCustomer(order().getCustomer());
		request.setType(CommerceOrderResolutionRequestType.CANCELLATION);
		request.setStatus(CommerceOrderResolutionRequestStatus.PENDING);
		request.setReason("Changed mind");
		request.setRefundFailureCode("REFUND_FAILED");
		request.setRefundFailureMessageKey("commerce.payment.refund.failed");
		return request;
	}

	private CommerceOrder order() {
		CommerceOrder order = new CommerceOrder();
		order.setId(1L);
		order.setUid("order-uid");
		order.setOrderNumber("ORD-1");
		order.setCustomer(customer());
		order.setStatus(CommerceOrderStatus.PAID);
		order.setCurrencyIso("TRY");
		order.setTotal(BigDecimal.valueOf(200).setScale(2));
		return order;
	}

	private CommercePaymentAttempt paymentAttempt() {
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setId(40L);
		attempt.setUid("attempt-uid");
		attempt.setCustomer(customer());
		attempt.setFailureCode("PROVIDER_RETRIEVE_FAILED");
		attempt.setFailureMessageKey("commerce.payment.provider.retrieve.failed");
		return attempt;
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("jane@example.com");
		customer.setFirstName("Jane");
		customer.setLastName("Doe");
		return customer;
	}

	private User activeAdmin(String email) {
		User user = new User();
		user.setEmail(email);
		user.setRole(UserRole.TENANT_ADMIN);
		user.setIsActive(true);
		return user;
	}

	private User inactiveAdmin(String email) {
		User user = activeAdmin(email);
		user.setIsActive(false);
		return user;
	}
}
