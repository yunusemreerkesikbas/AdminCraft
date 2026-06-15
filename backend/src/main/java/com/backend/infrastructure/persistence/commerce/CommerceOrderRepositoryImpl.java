package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.repository.CommerceOrderRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceOrderRepositoryImpl implements CommerceOrderRepository {

	private final CommerceOrderJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceOrder save(CommerceOrder order) {
		return jpaRepository.save(order);
	}

	@Override
	public Optional<CommerceOrder> findByPaymentAttemptId(Long paymentAttemptId) {
		return jpaRepository.findByPaymentAttemptId(paymentAttemptId);
	}

	@Override
	public Optional<CommerceOrder> findByCheckoutId(Long checkoutId) {
		return jpaRepository.findByCheckoutId(checkoutId);
	}

	@Override
	public Page<CommerceOrder> findByCustomerId(Long customerId, Pageable pageable) {
		return jpaRepository.findByCustomerId(customerId, pageable);
	}

	@Override
	public Optional<CommerceOrder> findByCustomerIdAndUid(Long customerId, String uid) {
		return jpaRepository.findByCustomerIdAndUid(customerId, uid);
	}

	@Override
	public Map<Long, Integer> countItemsByOrderIds(List<Long> orderIds) {
		if (orderIds == null || orderIds.isEmpty()) {
			return Map.of();
		}
		return jpaRepository.countItemsByOrderIds(orderIds).stream()
				.collect(Collectors.toMap(
						CommerceOrderJpaRepository.OrderItemCount::getOrderId,
						count -> Math.toIntExact(count.getItemCount())));
	}
}
