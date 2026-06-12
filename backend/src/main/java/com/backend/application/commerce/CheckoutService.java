package com.backend.application.commerce;

import com.backend.application.commerce.dto.CheckoutAddressSelectionCommand;
import com.backend.application.commerce.dto.CheckoutResponse;

public interface CheckoutService extends CommerceApplicationService {

	CheckoutResponse start(CommerceCustomerPrincipal principal, CheckoutAddressSelectionCommand command);

	CheckoutResponse getCurrent(CommerceCustomerPrincipal principal);

	CheckoutResponse updateAddresses(
			CommerceCustomerPrincipal principal,
			String checkoutUid,
			CheckoutAddressSelectionCommand command);
}
