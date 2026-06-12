package com.backend.application.commerce;

import com.backend.application.commerce.dto.CommerceCustomerResponse;
import com.backend.application.commerce.dto.UpdateCommerceCustomerProfileCommand;

public interface CommerceCustomerProfileService extends CommerceApplicationService {

	CommerceCustomerResponse getMe(CommerceCustomerPrincipal principal);

	CommerceCustomerResponse updateMe(CommerceCustomerPrincipal principal, UpdateCommerceCustomerProfileCommand command);
}
