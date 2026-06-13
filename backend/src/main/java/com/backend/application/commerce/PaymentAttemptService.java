package com.backend.application.commerce;

import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;

public interface PaymentAttemptService {

	PaymentAttemptResponse create(CommerceCustomerPrincipal principal, CreatePaymentAttemptCommand command);

	PaymentAttemptResponse get(CommerceCustomerPrincipal principal, String attemptUid);
}
