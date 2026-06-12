package com.backend.application.commerce;

import java.util.List;

import com.backend.application.commerce.dto.CommerceCustomerAddressCommand;
import com.backend.application.commerce.dto.CommerceCustomerAddressResponse;

public interface CommerceCustomerAddressService extends CommerceApplicationService {

	List<CommerceCustomerAddressResponse> list(CommerceCustomerPrincipal principal);

	CommerceCustomerAddressResponse create(CommerceCustomerPrincipal principal, CommerceCustomerAddressCommand command);

	CommerceCustomerAddressResponse update(CommerceCustomerPrincipal principal, String addressUid, CommerceCustomerAddressCommand command);

	void delete(CommerceCustomerPrincipal principal, String addressUid);

	CommerceCustomerAddressResponse setDefaultDelivery(CommerceCustomerPrincipal principal, String addressUid);

	CommerceCustomerAddressResponse setDefaultBilling(CommerceCustomerPrincipal principal, String addressUid);
}
