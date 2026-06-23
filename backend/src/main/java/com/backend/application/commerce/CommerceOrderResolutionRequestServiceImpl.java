package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.backend.application.commerce.CommercePaymentProviderPort.RefundPaymentCommand;
import com.backend.application.commerce.CommercePaymentProviderPort.RefundPaymentResult;
import com.backend.application.commerce.dto.CommerceOrderResolutionDecisionCommand;
import com.backend.application.commerce.dto.CommerceOrderResolutionRequestResponse;
import com.backend.application.commerce.dto.CreateCommerceOrderResolutionRequestCommand;
import com.backend.application.commerce.dto.CustomerOrderResolutionRequestResponse;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderItem;
import com.backend.domain.commerce.CommerceOrderResolutionRefundStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommerceOrderStatusHistory;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.commerce.repository.CommerceOrderResolutionRequestRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.shared.common.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceOrderResolutionRequestServiceImpl implements CommerceOrderResolutionRequestService {

	private static final Logger log = LoggerFactory.getLogger(CommerceOrderResolutionRequestServiceImpl.class);

	private static final String ORDER_NOT_FOUND = "commerce.order.not.found";
	private static final String REQUEST_NOT_FOUND = "commerce.order.request.not.found";
	private static final String REQUEST_NOT_ELIGIBLE = "commerce.order.request.not.eligible";
	private static final String REQUEST_PENDING_EXISTS = "commerce.order.request.pending.exists";
	private static final String REQUEST_NOT_PENDING = "commerce.order.request.not.pending";
	private static final String REQUEST_REFUND_PROCESSING = "commerce.order.request.refund.processing";
	private static final String REFUND_FAILED = "commerce.payment.refund.failed";
	private static final String STOCK_RESTORE_FAILED = "commerce.order.attention.stock_restore_failed";
	private static final String DEFAULT_CLIENT_IP = "127.0.0.1";

	private final CommerceOrderRepository orderRepository;
	private final CommerceOrderResolutionRequestRepository requestRepository;
	private final CommerceProductVariantStockPort stockPort;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final List<CommercePaymentProviderPort> paymentProviders;
	private final CommercePaymentConfigResolver paymentConfigResolver;
	private final TransactionTemplate transactionTemplate;
	private final CommerceNotificationService notificationService;
	private final CommerceAdminNotificationService adminNotificationService;

	@Override
	@Transactional
	public CustomerOrderResolutionRequestResponse createCustomerRequest(
			CommerceCustomerPrincipal principal,
			String orderUid,
			CreateCommerceOrderResolutionRequestCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceOrder order = orderRepository.findByCustomerIdAndUidForUpdate(principal.customerId(), orderUid)
				.orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND));
		CommerceOrderResolutionRequestType type = command.type();
		assertEligible(order, type);
		if (requestRepository.existsPendingByOrderId(order.getId())) {
			throw new CommerceDomainException(REQUEST_PENDING_EXISTS);
		}
		CommerceOrderStatus previousStatus = order.getStatus();
		CommerceOrderStatus requestedStatus = requestedStatus(type);
		order.setStatus(requestedStatus);
		order.setStatusChangedAt(LocalDateTime.now());
		order.addStatusHistory(history(previousStatus, requestedStatus, "commerce.order.request.created"));

		CommerceOrderResolutionRequest request = new CommerceOrderResolutionRequest();
		request.setOrder(order);
		request.setCustomer(order.getCustomer());
		request.setType(type);
		request.setStatus(CommerceOrderResolutionRequestStatus.PENDING);
		request.setReason(normalizeRequired(command.reason()));
		request.setDescription(normalizeRequired(command.description()));
		request.setPreviousOrderStatus(previousStatus);
		request.setRequestedOrderStatus(requestedStatus);
		request.setRefundStatus(CommerceOrderResolutionRefundStatus.NOT_ATTEMPTED);
		CommerceOrderResolutionRequest saved = requestRepository.save(request);
		notificationService.notifyOrderRequestCreated(saved);
		adminNotificationService.notifyOrderRequestCreated(saved);
		return CustomerOrderResolutionRequestResponse.from(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomerOrderResolutionRequestResponse> listCustomerRequests(
			CommerceCustomerPrincipal principal,
			String orderUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return requestRepository.findByCustomerIdAndOrderUid(principal.customerId(), orderUid).stream()
				.map(CustomerOrderResolutionRequestResponse::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CommerceOrderResolutionRequestResponse> listAdminRequests(
			Pageable pageable,
			String search,
			CommerceOrderResolutionRequestType type,
			CommerceOrderResolutionRequestStatus status) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return requestRepository.findAdminRequests(normalizeSearch(search), type, status, pageable)
				.map(CommerceOrderResolutionRequestResponse::from);
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceOrderResolutionRequestResponse getAdminRequest(String requestUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return requestRepository.findAdminByUid(requestUid)
				.map(CommerceOrderResolutionRequestResponse::from)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
	}

	@Override
	public CommerceOrderResolutionRequestResponse decide(
			String requestUid,
			CommerceOrderResolutionDecisionCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		if (!command.approve()) {
			return inTransaction(() -> reject(requestUid, command.decisionNote()));
		}
		RefundAttemptContext context = inTransaction(() -> beginRefundAttempt(requestUid, command.decisionNote()));
		if (!context.refundAlreadySucceeded()) {
			RefundPaymentResult result = refund(context);
			if (!result.successful()) {
				return inTransaction(() -> recordFailedRefundAttempt(context.requestUid(), result));
			}
			inTransaction(() -> {
				recordSuccessfulRefundAttempt(context.requestUid(), result);
				return true;
			});
		}
		return inTransaction(() -> completeRefundAttempt(context.requestUid()));
	}

	private RefundAttemptContext beginRefundAttempt(String requestUid, String note) {
		CommerceOrderResolutionRequest request = requestRepository.findByUidForUpdate(requestUid)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
		assertDecidable(request);
		if (request.getRefundStatus() == CommerceOrderResolutionRefundStatus.SUCCEEDED) {
			request.setDecisionNote(normalizeOptional(note));
			requestRepository.save(request);
			return new RefundAttemptContext(
					request.getUid(),
					request.getRefundProvider(),
					request.getOrder().getProviderTransactionId(),
					money(request.getOrder().getTotal()),
					request.getOrder().getCurrencyIso(),
					request.getReason(),
					request.getDescription(),
					true);
		}
		CommerceOrder order = request.getOrder();
		LocalDateTime now = LocalDateTime.now();
		request.setDecisionNote(normalizeOptional(note));
		request.setRefundProvider(order.getProvider());
		request.setRefundAttemptedAt(now);
		request.setRefundStatus(CommerceOrderResolutionRefundStatus.PROCESSING);
		request.setRefundReference(null);
		request.setRefundFailureCode(null);
		request.setRefundFailureMessageKey(null);
		requestRepository.save(request);
		return new RefundAttemptContext(
				request.getUid(),
				order.getProvider(),
				order.getProviderTransactionId(),
				money(order.getTotal()),
				order.getCurrencyIso(),
				request.getReason(),
				request.getDescription(),
				false);
	}

	private RefundPaymentResult refund(RefundAttemptContext context) {
		try {
			return paymentProvider(context.provider()).refundPayment(new RefundPaymentCommand(
					paymentConfigResolver.credentialsForProvider(context.provider()),
					context.requestUid(),
					context.paymentId(),
					context.amount(),
					context.currencyIso(),
					DEFAULT_CLIENT_IP,
					context.reason(),
					context.description()));
		} catch (CommercePaymentProviderException | IllegalArgumentException | IllegalStateException ex) {
			log.warn(
					"Commerce refund attempt failed for requestUid={} provider={} reason={}",
					context.requestUid(),
					context.provider(),
					ex.getMessage());
			return new RefundPaymentResult(false, null, "PROVIDER_REFUND_FAILED", REFUND_FAILED);
		}
	}

	private CommerceOrderResolutionRequestResponse recordFailedRefundAttempt(String requestUid, RefundPaymentResult result) {
		CommerceOrderResolutionRequest request = requestRepository.findByUidForUpdate(requestUid)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
		if (request.getStatus() != CommerceOrderResolutionRequestStatus.PENDING) {
			throw new CommerceDomainException(REQUEST_NOT_PENDING);
		}
		if (request.getRefundStatus() != CommerceOrderResolutionRefundStatus.PROCESSING) {
			throw new CommerceDomainException(REQUEST_REFUND_PROCESSING);
		}
		request.setRefundStatus(CommerceOrderResolutionRefundStatus.FAILED);
		request.setRefundReference(null);
		request.setRefundFailureCode(result.failureCode());
		request.setRefundFailureMessageKey(nonBlankOrDefault(result.failureMessageKey(), REFUND_FAILED));
		CommerceOrderResolutionRequest saved = requestRepository.save(request);
		adminNotificationService.notifyRefundOperationFailed(saved);
		return CommerceOrderResolutionRequestResponse.from(saved);
	}

	private void recordSuccessfulRefundAttempt(String requestUid, RefundPaymentResult result) {
		CommerceOrderResolutionRequest request = requestRepository.findByUidForUpdate(requestUid)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
		if (request.getStatus() != CommerceOrderResolutionRequestStatus.PENDING) {
			throw new CommerceDomainException(REQUEST_NOT_PENDING);
		}
		if (request.getRefundStatus() != CommerceOrderResolutionRefundStatus.PROCESSING) {
			throw new CommerceDomainException(REQUEST_REFUND_PROCESSING);
		}
		LocalDateTime now = LocalDateTime.now();
		request.setRefundStatus(CommerceOrderResolutionRefundStatus.SUCCEEDED);
		request.setRefundReference(result.refundReference());
		request.setRefundFailureCode(null);
		request.setRefundFailureMessageKey(null);
		request.setRefundedAt(now);
		requestRepository.save(request);
	}

	private CommerceOrderResolutionRequestResponse completeRefundAttempt(String requestUid) {
		CommerceOrderResolutionRequest request = requestRepository.findByUidForUpdate(requestUid)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
		if (request.getStatus() != CommerceOrderResolutionRequestStatus.PENDING) {
			throw new CommerceDomainException(REQUEST_NOT_PENDING);
		}
		if (request.getRefundStatus() != CommerceOrderResolutionRefundStatus.SUCCEEDED) {
			throw new CommerceDomainException(REQUEST_REFUND_PROCESSING);
		}

		LocalDateTime now = LocalDateTime.now();
		CommerceOrder order = request.getOrder();
		request.setStatus(CommerceOrderResolutionRequestStatus.APPROVED);
		request.setDecidedAt(now);
		request.setDecidedByUserId(SecurityUtil.getCurrentUserId());
		request.setDecidedByEmail(SecurityUtil.getCurrentUserEmail());
		CommerceOrderStatus finalStatus = finalStatus(request.getType());
		CommerceOrderStatus fromStatus = order.getStatus();
		order.setStatus(finalStatus);
		order.setStatusChangedAt(now);
		order.addStatusHistory(history(fromStatus, finalStatus, request.getDecisionNote()));
		restoreStockIfNeeded(request);
		CommerceOrderResolutionRequest saved = requestRepository.save(request);
		notificationService.notifyOrderRequestDecided(saved);
		return CommerceOrderResolutionRequestResponse.from(saved);
	}

	private CommerceOrderResolutionRequestResponse reject(String requestUid, String note) {
		CommerceOrderResolutionRequest request = requestRepository.findByUidForUpdate(requestUid)
				.orElseThrow(() -> new EntityNotFoundException(REQUEST_NOT_FOUND));
		assertRejectable(request);
		LocalDateTime now = LocalDateTime.now();
		CommerceOrder order = request.getOrder();
		CommerceOrderStatus fromStatus = order.getStatus();
		order.setStatus(request.getPreviousOrderStatus());
		order.setStatusChangedAt(now);
		order.addStatusHistory(history(fromStatus, request.getPreviousOrderStatus(), normalizeOptional(note)));
		request.setStatus(CommerceOrderResolutionRequestStatus.REJECTED);
		request.setDecisionNote(normalizeOptional(note));
		request.setDecidedAt(now);
		request.setDecidedByUserId(SecurityUtil.getCurrentUserId());
		request.setDecidedByEmail(SecurityUtil.getCurrentUserEmail());
		CommerceOrderResolutionRequest saved = requestRepository.save(request);
		notificationService.notifyOrderRequestDecided(saved);
		return CommerceOrderResolutionRequestResponse.from(saved);
	}

	private void assertDecidable(CommerceOrderResolutionRequest request) {
		if (request.getStatus() != CommerceOrderResolutionRequestStatus.PENDING) {
			throw new CommerceDomainException(REQUEST_NOT_PENDING);
		}
		if (request.getRefundStatus() == CommerceOrderResolutionRefundStatus.PROCESSING) {
			throw new CommerceDomainException(REQUEST_REFUND_PROCESSING);
		}
	}

	private void assertRejectable(CommerceOrderResolutionRequest request) {
		if (request.getStatus() != CommerceOrderResolutionRequestStatus.PENDING) {
			throw new CommerceDomainException(REQUEST_NOT_PENDING);
		}
		if (request.getRefundStatus() == CommerceOrderResolutionRefundStatus.PROCESSING
				|| request.getRefundStatus() == CommerceOrderResolutionRefundStatus.SUCCEEDED) {
			throw new CommerceDomainException(REQUEST_REFUND_PROCESSING);
		}
	}

	private void restoreStockIfNeeded(CommerceOrderResolutionRequest request) {
		CommerceOrder order = request.getOrder();
		if (request.getType() != CommerceOrderResolutionRequestType.CANCELLATION
				|| !order.isStockDeducted()
				|| request.isStockRestored()) {
			return;
		}
		CommerceProductVariantStockPort.StockAdjustmentResult result = stockPort.restore(variantQuantities(order));
		if (result.success()) {
			request.setStockRestored(true);
			return;
		}
		order.setRequiresAttention(true);
		order.setAttentionReasonKey(nonBlankOrDefault(result.reasonMessageKey(), STOCK_RESTORE_FAILED));
	}

	private void assertEligible(CommerceOrder order, CommerceOrderResolutionRequestType type) {
		if (type == CommerceOrderResolutionRequestType.CANCELLATION
				&& (order.getStatus() == CommerceOrderStatus.PAID || order.getStatus() == CommerceOrderStatus.PREPARING)) {
			return;
		}
		if (type == CommerceOrderResolutionRequestType.RETURN && order.getStatus() == CommerceOrderStatus.DELIVERED) {
			return;
		}
		throw new CommerceDomainException(REQUEST_NOT_ELIGIBLE);
	}

	private CommerceOrderStatus requestedStatus(CommerceOrderResolutionRequestType type) {
		return type == CommerceOrderResolutionRequestType.CANCELLATION
				? CommerceOrderStatus.CANCELLATION_REQUESTED
				: CommerceOrderStatus.RETURN_REQUESTED;
	}

	private CommerceOrderStatus finalStatus(CommerceOrderResolutionRequestType type) {
		return type == CommerceOrderResolutionRequestType.CANCELLATION
				? CommerceOrderStatus.CANCELLED
				: CommerceOrderStatus.REFUNDED;
	}

	private CommerceOrderStatusHistory history(CommerceOrderStatus from, CommerceOrderStatus to, String note) {
		CommerceOrderStatusHistory history = new CommerceOrderStatusHistory();
		history.setFromStatus(from);
		history.setToStatus(to);
		history.setInternalNote(note);
		history.setChangedByUserId(SecurityUtil.getCurrentUserId());
		history.setChangedByEmail(SecurityUtil.getCurrentUserEmail());
		return history;
	}

	private CommercePaymentProviderPort paymentProvider(String providerCode) {
		return paymentProviders.stream()
				.filter(provider -> provider.providerCode().equals(providerCode))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("commerce.payment.provider.unsupported"));
	}

	private Map<String, Integer> variantQuantities(CommerceOrder order) {
		return order.getItems().stream()
				.filter(item -> item.getVariantUid() != null)
				.collect(Collectors.toMap(
						CommerceOrderItem::getVariantUid,
						item -> Objects.requireNonNullElse(item.getQuantity(), 0),
						Integer::sum));
	}

	private String normalizeRequired(String value) {
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException("commerce.order.request.reason.required");
		}
		return value.trim();
	}

	private String normalizeOptional(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String normalizeSearch(String value) {
		return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
	}

	private String nonBlankOrDefault(String value, String defaultValue) {
		return StringUtils.hasText(value) ? value.trim() : defaultValue;
	}

	private BigDecimal money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private <T> T inTransaction(TransactionWork<T> work) {
		return Objects.requireNonNull(transactionTemplate.execute(status -> work.get()));
	}

	@FunctionalInterface
	private interface TransactionWork<T> {
		T get();
	}

	private record RefundAttemptContext(
			String requestUid,
			String provider,
			String paymentId,
			BigDecimal amount,
			String currencyIso,
			String reason,
			String description,
			boolean refundAlreadySucceeded) {
	}
}
