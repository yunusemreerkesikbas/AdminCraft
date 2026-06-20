package com.backend.application.commerce;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.commerce.dto.CommerceOrderResolutionDecisionCommand;
import com.backend.application.commerce.dto.CommerceOrderResolutionRequestResponse;
import com.backend.application.commerce.dto.CreateCommerceOrderResolutionRequestCommand;
import com.backend.application.commerce.dto.CustomerOrderResolutionRequestResponse;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

public interface CommerceOrderResolutionRequestService {

	CustomerOrderResolutionRequestResponse createCustomerRequest(
			CommerceCustomerPrincipal principal,
			String orderUid,
			CreateCommerceOrderResolutionRequestCommand command);

	List<CustomerOrderResolutionRequestResponse> listCustomerRequests(
			CommerceCustomerPrincipal principal,
			String orderUid);

	Page<CommerceOrderResolutionRequestResponse> listAdminRequests(
			Pageable pageable,
			String search,
			CommerceOrderResolutionRequestType type,
			CommerceOrderResolutionRequestStatus status);

	CommerceOrderResolutionRequestResponse getAdminRequest(String requestUid);

	CommerceOrderResolutionRequestResponse decide(
			String requestUid,
			CommerceOrderResolutionDecisionCommand command);
}
