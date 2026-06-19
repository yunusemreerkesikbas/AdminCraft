package com.backend.application.commerce;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.application.commerce.dto.CommerceAdminPaymentAttemptResponse;
import com.backend.application.commerce.dto.ChangeCommerceOrderStatusCommand;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;

import jakarta.validation.Valid;

public interface CommerceAdminOrderService {

	CommerceAdminDashboardResponse dashboard();

	Page<CommerceAdminOrderSummaryResponse> listOrders(
			Pageable pageable,
			String search,
			CommerceOrderStatus status,
			Boolean requiresAttention);

	CommerceAdminOrderDetailResponse getOrder(String orderUid);

	CommerceAdminOrderDetailResponse changeStatus(String orderUid, @Valid ChangeCommerceOrderStatusCommand command);

	Page<CommerceAdminPaymentAttemptResponse> listPaymentAttempts(
			Pageable pageable,
			String search,
			CommercePaymentAttemptStatus status);
}
