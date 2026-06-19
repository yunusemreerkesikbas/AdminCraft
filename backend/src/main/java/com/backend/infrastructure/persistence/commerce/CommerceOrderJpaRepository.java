package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderStatus;

import jakarta.persistence.LockModeType;

interface CommerceOrderJpaRepository extends JpaRepository<CommerceOrder, Long> {

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByPaymentAttemptId(Long paymentAttemptId);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByCheckoutId(Long checkoutId);

	Page<CommerceOrder> findByCustomerId(Long customerId, Pageable pageable);

	@EntityGraph(attributePaths = { "items" })
	Optional<CommerceOrder> findByCustomerIdAndUid(Long customerId, String uid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "items", "customer" })
	@Query("select o from CommerceOrder o where o.customer.id = :customerId and o.uid = :uid")
	Optional<CommerceOrder> findByCustomerIdAndUidForUpdate(@Param("customerId") Long customerId, @Param("uid") String uid);

	@EntityGraph(attributePaths = { "customer" })
	@Query("""
			select o from CommerceOrder o
			join o.customer customer
			where (:status is null or o.status = :status)
				and (:requiresAttention is null or o.requiresAttention = :requiresAttention)
				and (:search is null
					or lower(o.uid) like concat('%', :search, '%')
					or lower(o.orderNumber) like concat('%', :search, '%')
					or lower(o.provider) like concat('%', :search, '%')
					or lower(coalesce(o.providerTransactionId, '')) like concat('%', :search, '%')
					or lower(customer.email) like concat('%', :search, '%')
					or lower(customer.firstName) like concat('%', :search, '%')
					or lower(customer.lastName) like concat('%', :search, '%')
					or lower(customer.phone) like concat('%', :search, '%'))
			""")
	Page<CommerceOrder> findAdminOrders(
			@Param("search") String search,
			@Param("status") CommerceOrderStatus status,
			@Param("requiresAttention") Boolean requiresAttention,
			Pageable pageable);

	@EntityGraph(attributePaths = { "customer", "items", "paymentAttempt" })
	@Query("select o from CommerceOrder o where o.uid = :uid")
	Optional<CommerceOrder> findAdminByUid(@Param("uid") String uid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "customer", "items", "paymentAttempt" })
	@Query("select o from CommerceOrder o where o.uid = :uid")
	Optional<CommerceOrder> findAdminByUidForUpdate(@Param("uid") String uid);

	@Query("""
			select item.order.id as orderId, count(item.id) as itemCount
			from CommerceOrderItem item
			where item.order.id in :orderIds
			group by item.order.id
			""")
	List<OrderItemCount> countItemsByOrderIds(@Param("orderIds") List<Long> orderIds);

	long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

	@Query("""
			select coalesce(sum(o.total), 0)
			from CommerceOrder o
			where o.createdAt >= :start and o.createdAt < :end
			""")
	BigDecimal sumTotalByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	long countByRequiresAttentionTrue();

	@Query("select o.currencyIso from CommerceOrder o order by o.createdAt desc")
	List<String> findMostRecentCurrencyIso(Pageable pageable);

	interface OrderItemCount {
		Long getOrderId();

		long getItemCount();
	}
}
