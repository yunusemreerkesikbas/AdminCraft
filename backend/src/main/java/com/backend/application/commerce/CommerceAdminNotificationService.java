package com.backend.application.commerce;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommercePaymentAttempt;

public interface CommerceAdminNotificationService {

	void notifyOrderCreated(CommerceOrder order);

	void notifyOrderRequestCreated(CommerceOrderResolutionRequest request);

	void notifyPaymentOperationFailed(CommercePaymentAttempt attempt, String operationType);

	void notifyRefundOperationFailed(CommerceOrderResolutionRequest request);
}
