package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceOrder;

interface CommerceOrderJpaRepository extends JpaRepository<CommerceOrder, Long> {

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByPaymentAttemptId(Long paymentAttemptId);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByCheckoutId(Long checkoutId);

	Page<CommerceOrder> findByCustomerId(Long customerId, Pageable pageable);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByCustomerIdAndUid(Long customerId, String uid);

	@Query("""
			select item.order.id as orderId, count(item.id) as itemCount
			from CommerceOrderItem item
			where item.order.id in :orderIds
			group by item.order.id
			""")
	List<OrderItemCount> countItemsByOrderIds(@Param("orderIds") List<Long> orderIds);

	interface OrderItemCount {
		Long getOrderId();

		long getItemCount();
	}
}
