package com.backend.domain.commerce.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;

public interface CommercePaymentAttemptRepository {

	CommercePaymentAttempt save(CommercePaymentAttempt attempt);

	Optional<CommercePaymentAttempt> findByCustomerIdAndUid(Long customerId, String uid);

	Optional<CommercePaymentAttempt> findFirstByProviderAndProviderReference(String provider, String providerReference);

	int reservePendingAttemptInitialization(
			Long attemptId,
			CommercePaymentAttemptStatus pendingStatus,
			CommercePaymentAttemptStatus initializingStatus,
			LocalDateTime now);

	int expirePendingAttemptsForCheckout(Long customerId, Long checkoutId, CommercePaymentAttemptStatus pendingStatus);
}
