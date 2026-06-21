package com.backend.application.commerce;

import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;

public interface CommerceNotificationService {

	void notifyOrderPaid(CommerceOrder order);

	void notifyOrderShipped(CommerceOrder order);

	void notifyOrderRequestCreated(CommerceOrderResolutionRequest request);

	void notifyOrderRequestDecided(CommerceOrderResolutionRequest request);
}
