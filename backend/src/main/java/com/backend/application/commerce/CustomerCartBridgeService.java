package com.backend.application.commerce;

import com.backend.application.commerce.dto.CartMergeResponse;
import com.backend.application.commerce.dto.CartResponse;
import com.backend.domain.commerce.CommerceCustomer;

public interface CustomerCartBridgeService extends CommerceApplicationService {

	CustomerCartBridgeResult mergeOnAuth(CommerceCustomer customer, String sourceCartToken);

	record CustomerCartBridgeResult(CartResponse cart, CartMergeResponse merge) {
	}
}
