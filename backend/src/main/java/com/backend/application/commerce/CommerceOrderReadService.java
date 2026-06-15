package com.backend.application.commerce;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.commerce.dto.CommerceOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceOrderSummaryResponse;

public interface CommerceOrderReadService {

	Page<CommerceOrderSummaryResponse> list(CommerceCustomerPrincipal principal, Pageable pageable);

	CommerceOrderDetailResponse get(CommerceCustomerPrincipal principal, String orderUid);
}
