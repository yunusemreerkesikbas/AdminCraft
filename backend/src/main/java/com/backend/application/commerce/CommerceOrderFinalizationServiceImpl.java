package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderItem;
import com.backend.domain.commerce.CommerceOrderLegalSnapshotStatus;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommerceOrderNumberCounterRepository;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.port.TenantContextPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceOrderFinalizationServiceImpl implements CommerceOrderFinalizationService {

	private static final String ORDER_NUMBER_PREFIX_KEY = "commerce.order.number_prefix";
	private static final String DEFAULT_ORDER_PREFIX = "ORD";
	private static final String ORDER_PREFIX_PATTERN = "[A-Z0-9]{1,20}";
	private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private static final String STOCK_ATTENTION_KEY = "commerce.order.attention.stock_not_deducted";
	private static final String LEGAL_ATTENTION_KEY = "commerce.order.attention.legal_snapshot_not_captured";

	private final CommerceOrderRepository orderRepository;
	private final CommerceCheckoutRepository checkoutRepository;
	private final CommerceOrderNumberCounterRepository orderNumberCounterRepository;
	private final CommerceProductVariantStockPort stockPort;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final ObjectMapper objectMapper;
	private final CommerceNotificationService notificationService;

	@Override
	@Transactional
	public CommerceOrder finalizeSuccessfulPayment(CommercePaymentAttempt attempt) {
		if (attempt == null || attempt.getId() == null) {
			throw new IllegalArgumentException("commerce.payment.attempt.required");
		}
		if (attempt.getStatus() != CommercePaymentAttemptStatus.SUCCEEDED) {
			throw new IllegalStateException("commerce.payment.attempt.not.succeeded");
		}
		CommerceCheckout checkout = attempt.getCheckout();
		if (checkout == null || checkout.getId() == null) {
			throw new IllegalStateException("commerce.checkout.required");
		}
		CommerceCheckout lockedCheckout = checkoutRepository.findByIdForUpdate(checkout.getId())
				.orElseThrow(() -> new IllegalStateException("commerce.checkout.required"));
		Optional<CommerceOrder> existingByAttempt = orderRepository.findByPaymentAttemptId(attempt.getId());
		if (existingByAttempt.isPresent()) {
			return existingByAttempt.get();
		}
		Optional<CommerceOrder> existingByCheckout = orderRepository.findByCheckoutId(lockedCheckout.getId());
		if (existingByCheckout.isPresent()) {
			return existingByCheckout.get();
		}

		CommerceProductVariantStockPort.StockDeductionResult stockResult = stockPort.deductIfAvailable(variantQuantities(lockedCheckout));
		CommerceOrder order = buildOrder(attempt, lockedCheckout, stockResult);
		CommerceCart cart = lockedCheckout.getCart();
		if (cart != null) {
			cart.setStatus(CommerceCartStatus.CLEARED);
		}
		lockedCheckout.setStatus(CommerceCheckoutStatus.COMPLETED);
		CommerceOrder saved = orderRepository.save(order);
		notificationService.notifyOrderPaid(saved);
		return saved;
	}

	private CommerceOrder buildOrder(
			CommercePaymentAttempt attempt,
			CommerceCheckout checkout,
			CommerceProductVariantStockPort.StockDeductionResult stockResult) {
		CommerceOrder order = new CommerceOrder();
		order.setOrderNumber(nextOrderNumber());
		order.setCustomer(attempt.getCustomer());
		order.setCheckout(checkout);
		order.setPaymentAttempt(attempt);
		order.setStatus(CommerceOrderStatus.PAID);
		order.setCurrencyIso(checkout.getCurrencyIso());
		order.setSubtotal(money(checkout.getSubtotal()));
		order.setVatTotal(money(checkout.getVatTotal()));
		order.setShippingTotal(money(checkout.getShippingTotal()));
		order.setTotal(money(checkout.getTotal()));
		order.setShippingMethodCode(checkout.getShippingMethodCode());
		order.setShippingMethodName(checkout.getShippingMethodName());
		order.setDeliveryAddressUid(checkout.getDeliveryAddressUid());
		order.setBillingAddressUid(checkout.getBillingAddressUid());
		order.setDeliveryAddressSnapshot(checkout.getDeliveryAddressSnapshot());
		order.setBillingAddressSnapshot(checkout.getBillingAddressSnapshot());
		order.setProvider(attempt.getProvider());
		order.setProviderTransactionId(attempt.getProviderTransactionId());
		String legalSnapshotJson = orderLegalSnapshotJson(attempt, order);
		boolean legalCaptured = legalSnapshotJson != null;
		order.setLegalSnapshotStatus(legalCaptured
				? CommerceOrderLegalSnapshotStatus.CAPTURED
				: CommerceOrderLegalSnapshotStatus.NOT_CAPTURED);
		order.setLegalSnapshotJson(legalSnapshotJson);
		order.setStockDeducted(stockResult.success());
		order.setRequiresAttention(!stockResult.success() || !legalCaptured);
		order.setAttentionReasonKey(attentionReason(stockResult, legalCaptured));
		checkout.getItems().forEach(item -> order.addItem(orderItem(item)));
		return order;
	}

	private String attentionReason(
			CommerceProductVariantStockPort.StockDeductionResult stockResult,
			boolean legalCaptured) {
		if (!stockResult.success()) {
			return Optional.ofNullable(stockResult.reasonMessageKey()).orElse(STOCK_ATTENTION_KEY);
		}
		if (!legalCaptured) {
			return LEGAL_ATTENTION_KEY;
		}
		return null;
	}

	private String orderLegalSnapshotJson(CommercePaymentAttempt attempt, CommerceOrder order) {
		if (attempt.getLegalAcceptanceJson() == null || attempt.getLegalAcceptanceJson().isBlank()) {
			return null;
		}
		try {
			JsonNode rawSnapshot = objectMapper.readTree(attempt.getLegalAcceptanceJson());
			if (!(rawSnapshot instanceof ObjectNode snapshot)) {
				return null;
			}
			ObjectNode orderNode = snapshot.putObject("order");
			orderNode.put("orderNumber", order.getOrderNumber());
			orderNode.put("status", order.getStatus().name());
			ObjectNode paymentNode = snapshot.putObject("payment");
			paymentNode.put("attemptUid", attempt.getUid());
			paymentNode.put("provider", attempt.getProvider());
			paymentNode.put("providerTransactionId", attempt.getProviderTransactionId());
			if (attempt.getLegalAcceptanceCapturedAt() != null) {
				paymentNode.put("legalAcceptanceCapturedAt", attempt.getLegalAcceptanceCapturedAt().toString());
			}
			return objectMapper.writeValueAsString(snapshot);
		} catch (JsonProcessingException | ClassCastException ex) {
			return null;
		}
	}

	private CommerceOrderItem orderItem(CommerceCheckoutItem checkoutItem) {
		CommerceOrderItem item = new CommerceOrderItem();
		item.setProductUid(checkoutItem.getProductUid());
		item.setProductSku(checkoutItem.getProductSku());
		item.setVariantUid(checkoutItem.getVariantUid());
		item.setVariantSku(checkoutItem.getVariantSku());
		item.setQuantity(checkoutItem.getQuantity());
		item.setUnitGrossPrice(money(checkoutItem.getUnitGrossPrice()));
		item.setVatRate(checkoutItem.getVatRate());
		item.setLineTotal(money(checkoutItem.getLineTotal()));
		item.setLineVatTotal(money(checkoutItem.getLineVatTotal()));
		return item;
	}

	private Map<String, Integer> variantQuantities(CommerceCheckout checkout) {
		return checkout.getItems().stream()
				.filter(item -> item.getVariantUid() != null)
				.collect(Collectors.toMap(
						CommerceCheckoutItem::getVariantUid,
						item -> Objects.requireNonNullElse(item.getQuantity(), 0),
						Integer::sum));
	}

	private String nextOrderNumber() {
		LocalDate orderDate = LocalDateTime.now().toLocalDate();
		String prefix = orderPrefix();
		int sequence = orderNumberCounterRepository.nextSequence(prefix, orderDate);
		return prefix + "-" + ORDER_DATE_FORMAT.format(orderDate) + "-" + String.format("%06d", sequence);
	}

	private String orderPrefix() {
		return configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), ORDER_NUMBER_PREFIX_KEY)
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.map(value -> value.toUpperCase(Locale.ROOT))
				.filter(value -> value.matches(ORDER_PREFIX_PATTERN))
				.orElse(DEFAULT_ORDER_PREFIX);
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}

	private BigDecimal money(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}
}
