package com.backend.infrastructure.persistence.commerce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;
import com.backend.domain.commerce.repository.CommerceOrderResolutionRequestRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceOrderResolutionRequestRepositoryImpl implements CommerceOrderResolutionRequestRepository {

	private final CommerceOrderResolutionRequestJpaRepository jpaRepository;

	@Override
	public CommerceOrderResolutionRequest save(CommerceOrderResolutionRequest request) {
		return jpaRepository.save(request);
	}

	@Override
	public boolean existsPendingByOrderId(Long orderId) {
		return jpaRepository.existsByOrderIdAndStatus(orderId, CommerceOrderResolutionRequestStatus.PENDING);
	}

	@Override
	public List<CommerceOrderResolutionRequest> findByCustomerIdAndOrderUid(Long customerId, String orderUid) {
		return jpaRepository.findByCustomerIdAndOrderUidOrderByCreatedAtDesc(customerId, orderUid);
	}

	@Override
	public Page<CommerceOrderResolutionRequest> findAdminRequests(
			String search,
			CommerceOrderResolutionRequestType type,
			CommerceOrderResolutionRequestStatus status,
			Pageable pageable) {
		return jpaRepository.findAdminRequests(search, type, status, pageable);
	}

	@Override
	public Optional<CommerceOrderResolutionRequest> findAdminByUid(String uid) {
		return jpaRepository.findByUid(uid);
	}

	@Override
	public Optional<CommerceOrderResolutionRequest> findByUidForUpdate(String uid) {
		return jpaRepository.findByUidForUpdate(uid);
	}
}
