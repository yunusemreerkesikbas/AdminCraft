package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;

public interface CommercePaymentAttemptRepository {

	CommercePaymentAttempt save(CommercePaymentAttempt attempt);

	Optional<CommercePaymentAttempt> findByCustomerIdAndUid(Long customerId, String uid);

	int expirePendingAttemptsForCheckout(Long customerId, Long checkoutId, CommercePaymentAttemptStatus pendingStatus);
}
