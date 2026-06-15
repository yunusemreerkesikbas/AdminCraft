package com.backend.application.commerce;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CheckoutAddressSnapshotResponse;
import com.backend.application.commerce.dto.CommerceOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceOrderSummaryResponse;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceOrderReadServiceImpl implements CommerceOrderReadService {

	private static final String ORDER_NOT_FOUND = "commerce.order.not.found";
	private static final String ADDRESS_SNAPSHOT_INVALID = "commerce.checkout.address.snapshot.invalid";

	private final CommerceOrderRepository orderRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional(readOnly = true)
	public Page<CommerceOrderSummaryResponse> list(CommerceCustomerPrincipal principal, Pageable pageable) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		Page<CommerceOrder> orders = orderRepository.findByCustomerId(principal.customerId(), pageable);
		List<Long> orderIds = orders.getContent().stream()
				.map(CommerceOrder::getId)
				.toList();
		Map<Long, Integer> itemCounts = orderRepository.countItemsByOrderIds(orderIds);
		return orders.map(order -> CommerceOrderSummaryResponse.from(
				order,
				itemCounts.getOrDefault(order.getId(), 0)));
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceOrderDetailResponse get(CommerceCustomerPrincipal principal, String orderUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceOrder order = orderRepository.findByCustomerIdAndUid(principal.customerId(), orderUid)
				.orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND));
		return CommerceOrderDetailResponse.from(
				order,
				addressSnapshot(order.getDeliveryAddressSnapshot()),
				addressSnapshot(order.getBillingAddressSnapshot()));
	}

	private CheckoutAddressSnapshotResponse addressSnapshot(String json) {
		try {
			return objectMapper.readValue(json, CheckoutAddressSnapshotResponse.class);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException(ADDRESS_SNAPSHOT_INVALID, ex);
		}
	}
}
