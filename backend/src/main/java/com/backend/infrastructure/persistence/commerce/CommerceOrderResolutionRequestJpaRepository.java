package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

import jakarta.persistence.LockModeType;

interface CommerceOrderResolutionRequestJpaRepository extends JpaRepository<CommerceOrderResolutionRequest, Long> {

	boolean existsByOrderIdAndStatus(Long orderId, CommerceOrderResolutionRequestStatus status);

	@EntityGraph(attributePaths = { "order", "customer" })
	List<CommerceOrderResolutionRequest> findByCustomerIdAndOrderUidOrderByCreatedAtDesc(Long customerId, String orderUid);

	@EntityGraph(attributePaths = { "order", "customer" })
	@Query("""
			select request from CommerceOrderResolutionRequest request
			join request.order order
			join request.customer customer
			where (:type is null or request.type = :type)
				and (:status is null or request.status = :status)
				and (:search is null
					or lower(request.uid) like concat('%', :search, '%')
					or lower(order.uid) like concat('%', :search, '%')
					or lower(order.orderNumber) like concat('%', :search, '%')
					or lower(customer.email) like concat('%', :search, '%')
					or lower(customer.firstName) like concat('%', :search, '%')
					or lower(customer.lastName) like concat('%', :search, '%')
					or lower(request.reason) like concat('%', :search, '%'))
			""")
	Page<CommerceOrderResolutionRequest> findAdminRequests(
			@Param("search") String search,
			@Param("type") CommerceOrderResolutionRequestType type,
			@Param("status") CommerceOrderResolutionRequestStatus status,
			Pageable pageable);

	@EntityGraph(attributePaths = { "order", "customer" })
	Optional<CommerceOrderResolutionRequest> findByUid(String uid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "order", "order.items", "customer" })
	@Query("select request from CommerceOrderResolutionRequest request where request.uid = :uid")
	Optional<CommerceOrderResolutionRequest> findByUidForUpdate(@Param("uid") String uid);
}
