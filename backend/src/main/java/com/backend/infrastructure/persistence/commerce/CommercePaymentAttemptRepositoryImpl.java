package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommercePaymentAttemptRepositoryImpl implements CommercePaymentAttemptRepository {

	private final CommercePaymentAttemptJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommercePaymentAttempt save(CommercePaymentAttempt attempt) {
		return jpaRepository.save(attempt);
	}

	@Override
	public Optional<CommercePaymentAttempt> findByCustomerIdAndUid(Long customerId, String uid) {
		return jpaRepository.findByCustomerIdAndUid(customerId, uid);
	}

	@Override
	@Transactional
	public int expirePendingAttemptsForCheckout(
			Long customerId,
			Long checkoutId,
			CommercePaymentAttemptStatus pendingStatus) {
		return jpaRepository.expirePendingAttemptsForCheckout(customerId, checkoutId, pendingStatus);
	}
}
