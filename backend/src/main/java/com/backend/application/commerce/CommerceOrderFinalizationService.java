package com.backend.application.commerce;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommercePaymentAttempt;

public interface CommerceOrderFinalizationService {

	CommerceOrder finalizeSuccessfulPayment(CommercePaymentAttempt attempt);
}
