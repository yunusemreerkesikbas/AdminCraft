package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

public interface CommerceOrderResolutionRequestRepository {

	CommerceOrderResolutionRequest save(CommerceOrderResolutionRequest request);

	boolean existsPendingByOrderId(Long orderId);

	List<CommerceOrderResolutionRequest> findByCustomerIdAndOrderUid(Long customerId, String orderUid);

	Page<CommerceOrderResolutionRequest> findAdminRequests(
			String search,
			CommerceOrderResolutionRequestType type,
			CommerceOrderResolutionRequestStatus status,
			Pageable pageable);

	Optional<CommerceOrderResolutionRequest> findAdminByUid(String uid);

	Optional<CommerceOrderResolutionRequest> findByUidForUpdate(String uid);
}
