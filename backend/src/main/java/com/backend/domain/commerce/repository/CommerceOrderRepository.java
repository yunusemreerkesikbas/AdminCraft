package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderStatus;

public interface CommerceOrderRepository {

	CommerceOrder save(CommerceOrder order);

	void flush();

	Optional<CommerceOrder> findByPaymentAttemptId(Long paymentAttemptId);

	Optional<CommerceOrder> findByCheckoutId(Long checkoutId);

	Page<CommerceOrder> findByCustomerId(Long customerId, Pageable pageable);

	Optional<CommerceOrder> findByCustomerIdAndUid(Long customerId, String uid);

	Optional<CommerceOrder> findByCustomerIdAndUidForUpdate(Long customerId, String uid);

	Page<CommerceOrder> findAdminOrders(
			String search,
			CommerceOrderStatus status,
			Boolean requiresAttention,
			Pageable pageable);

	Optional<CommerceOrder> findAdminByUid(String uid);

	Optional<CommerceOrder> findAdminByUidForUpdate(String uid);

	Map<Long, Integer> countItemsByOrderIds(List<Long> orderIds);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	BigDecimal sumTotalByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	long countByRequiresAttentionTrue();

	Optional<String> findMostRecentCurrencyIso();
}
