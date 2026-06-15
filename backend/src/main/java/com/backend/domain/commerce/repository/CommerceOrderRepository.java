package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.commerce.CommerceOrder;

public interface CommerceOrderRepository {

	CommerceOrder save(CommerceOrder order);

	Optional<CommerceOrder> findByPaymentAttemptId(Long paymentAttemptId);

	Optional<CommerceOrder> findByCheckoutId(Long checkoutId);

	Page<CommerceOrder> findByCustomerId(Long customerId, Pageable pageable);

	Optional<CommerceOrder> findByCustomerIdAndUid(Long customerId, String uid);

	Map<Long, Integer> countItemsByOrderIds(List<Long> orderIds);
}
