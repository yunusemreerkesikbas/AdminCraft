package com.backend.application.commerce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.backend.application.commerce.dto.CheckoutAddressSnapshotResponse;
import com.backend.application.commerce.dto.ChangeCommerceOrderStatusCommand;
import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminMetricResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.application.commerce.dto.CommerceAdminPaymentAttemptResponse;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommerceOrderStatusHistory;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.shared.common.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
class CommerceAdminOrderServiceImpl implements CommerceAdminOrderService {

	private static final String ORDER_NOT_FOUND = "commerce.order.not.found";
	private static final String ADDRESS_SNAPSHOT_INVALID = "commerce.checkout.address.snapshot.invalid";
	private static final String INVALID_TRANSITION = "commerce.admin.order.status.transition.invalid";
	private static final String SHIPMENT_REQUIRED = "commerce.admin.order.status.shipment.required";
	private static final String DEFAULT_CURRENCY = "TRY";

	private final CommerceOrderRepository orderRepository;
	private final CommercePaymentAttemptRepository paymentAttemptRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final TenantContextPort tenantContext;
	private final ObjectMapper objectMapper;
	private final CommerceNotificationService notificationService;

	@Override
	@Transactional(readOnly = true)
	public CommerceAdminDashboardResponse dashboard() {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		LocalDate today = LocalDate.now();
		LocalDateTime todayStart = today.atStartOfDay();
		LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
		LocalDateTime sevenDaysStart = today.minusDays(6).atStartOfDay();
		String currencyIso = orderRepository.findMostRecentCurrencyIso().orElse(defaultCurrencyIso());

		CommerceAdminMetricResponse todayMetric = metric(todayStart, tomorrowStart, currencyIso);
		CommerceAdminMetricResponse lastSevenDaysMetric = metric(sevenDaysStart, tomorrowStart, currencyIso);
		long attentionOrderCount = orderRepository.countByRequiresAttentionTrue();
		long failedAttemptCount = paymentAttemptRepository.countByStatusAndCreatedAtGreaterThanEqual(
				CommercePaymentAttemptStatus.FAILED,
				sevenDaysStart);
		return new CommerceAdminDashboardResponse(
				todayMetric,
				lastSevenDaysMetric,
				attentionOrderCount,
				failedAttemptCount,
				currencyIso);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CommerceAdminOrderSummaryResponse> listOrders(
			Pageable pageable,
			String search,
			CommerceOrderStatus status,
			Boolean requiresAttention) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		Page<CommerceOrder> orders = orderRepository.findAdminOrders(
				normalize(search),
				status,
				requiresAttention,
				pageable);
		List<Long> orderIds = orders.getContent().stream()
				.map(CommerceOrder::getId)
				.toList();
		Map<Long, Integer> itemCounts = orderRepository.countItemsByOrderIds(orderIds);
		return orders.map(order -> CommerceAdminOrderSummaryResponse.from(
				order,
				itemCounts.getOrDefault(order.getId(), 0)));
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceAdminOrderDetailResponse getOrder(String orderUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceOrder order = orderRepository.findAdminByUid(orderUid)
				.orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND));
		return CommerceAdminOrderDetailResponse.from(
				order,
				addressSnapshot(order.getDeliveryAddressSnapshot()),
				addressSnapshot(order.getBillingAddressSnapshot()));
	}

	@Override
	@Transactional
	public CommerceAdminOrderDetailResponse changeStatus(String orderUid, @Valid ChangeCommerceOrderStatusCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceOrder order = orderRepository.findAdminByUidForUpdate(orderUid)
				.orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND));
		CommerceOrderStatus fromStatus = order.getStatus();
		CommerceOrderStatus toStatus = command.status();
		assertValidTransition(fromStatus, toStatus);

		String carrierName = normalizeOptional(command.carrierName());
		String trackingNumber = normalizeOptional(command.trackingNumber());
		String trackingUrl = normalizeOptional(command.trackingUrl());
		String internalNote = normalizeOptional(command.internalNote());
		if (toStatus == CommerceOrderStatus.SHIPPED && (!StringUtils.hasText(carrierName)
				|| !StringUtils.hasText(trackingNumber))) {
			throw new CommerceDomainException(SHIPMENT_REQUIRED);
		}

		LocalDateTime now = LocalDateTime.now();
		order.setStatus(toStatus);
		order.setStatusChangedAt(now);
		if (toStatus == CommerceOrderStatus.SHIPPED) {
			order.setShippingCarrierName(carrierName);
			order.setShippingTrackingNumber(trackingNumber);
			order.setShippingTrackingUrl(trackingUrl);
			order.setShippedAt(now);
		}
		if (toStatus == CommerceOrderStatus.DELIVERED) {
			order.setDeliveredAt(now);
		}

		CommerceOrderStatusHistory history = new CommerceOrderStatusHistory();
		history.setFromStatus(fromStatus);
		history.setToStatus(toStatus);
		history.setShippingCarrierName(toStatus == CommerceOrderStatus.SHIPPED ? carrierName : order.getShippingCarrierName());
		history.setShippingTrackingNumber(toStatus == CommerceOrderStatus.SHIPPED ? trackingNumber : order.getShippingTrackingNumber());
		history.setShippingTrackingUrl(toStatus == CommerceOrderStatus.SHIPPED ? trackingUrl : order.getShippingTrackingUrl());
		history.setInternalNote(internalNote);
		history.setChangedByUserId(SecurityUtil.getCurrentUserId());
		history.setChangedByEmail(SecurityUtil.getCurrentUserEmail());
		order.addStatusHistory(history);

		CommerceOrder saved = orderRepository.save(order);
		orderRepository.flush();
		if (toStatus == CommerceOrderStatus.SHIPPED) {
			notificationService.notifyOrderShipped(saved);
		}
		return CommerceAdminOrderDetailResponse.from(
				saved,
				addressSnapshot(saved.getDeliveryAddressSnapshot()),
				addressSnapshot(saved.getBillingAddressSnapshot()));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CommerceAdminPaymentAttemptResponse> listPaymentAttempts(
			Pageable pageable,
			String search,
			CommercePaymentAttemptStatus status) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return paymentAttemptRepository.findAdminPaymentAttempts(normalize(search), status, pageable)
				.map(CommerceAdminPaymentAttemptResponse::from);
	}

	private CommerceAdminMetricResponse metric(LocalDateTime start, LocalDateTime end, String currencyIso) {
		long orderCount = orderRepository.countByCreatedAtBetween(start, end);
		BigDecimal revenue = orderRepository.sumTotalByCreatedAtBetween(start, end);
		return new CommerceAdminMetricResponse(orderCount, revenue == null ? BigDecimal.ZERO : revenue, currencyIso);
	}

	private String normalize(String search) {
		if (!StringUtils.hasText(search)) {
			return null;
		}
		return search.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeOptional(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private void assertValidTransition(CommerceOrderStatus fromStatus, CommerceOrderStatus toStatus) {
		if (fromStatus == CommerceOrderStatus.PAID && toStatus == CommerceOrderStatus.PREPARING) {
			return;
		}
		if (fromStatus == CommerceOrderStatus.PREPARING && toStatus == CommerceOrderStatus.SHIPPED) {
			return;
		}
		if (fromStatus == CommerceOrderStatus.SHIPPED && toStatus == CommerceOrderStatus.DELIVERED) {
			return;
		}
		throw new CommerceDomainException(INVALID_TRANSITION);
	}

	private String defaultCurrencyIso() {
		if (tenantContext.getCurrency() == null) {
			return DEFAULT_CURRENCY;
		}
		return tenantContext.getCurrency().getIsoCode();
	}

	private CheckoutAddressSnapshotResponse addressSnapshot(String json) {
		try {
			return objectMapper.readValue(json, CheckoutAddressSnapshotResponse.class);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException(ADDRESS_SNAPSHOT_INVALID, ex);
		}
	}
}
