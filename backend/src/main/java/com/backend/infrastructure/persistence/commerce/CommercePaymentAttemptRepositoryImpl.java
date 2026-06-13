package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
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
	public Optional<CommercePaymentAttempt> findFirstByProviderAndProviderReference(String provider, String providerReference) {
		return jpaRepository.findFirstByProviderAndProviderReferenceOrderByIdDesc(provider, providerReference);
	}

	@Override
	@Transactional
	public int reservePendingAttemptInitialization(
			Long attemptId,
			CommercePaymentAttemptStatus pendingStatus,
			CommercePaymentAttemptStatus initializingStatus,
			LocalDateTime now) {
		return jpaRepository.reservePendingAttemptInitialization(attemptId, pendingStatus, initializingStatus, now);
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
