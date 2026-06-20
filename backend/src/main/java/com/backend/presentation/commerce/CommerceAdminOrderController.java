package com.backend.presentation.commerce;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceAdminOrderService;
import com.backend.application.commerce.CommerceOrderResolutionRequestService;
import com.backend.application.commerce.dto.ChangeCommerceOrderStatusCommand;
import com.backend.application.commerce.dto.CommerceOrderResolutionDecisionCommand;
import com.backend.application.commerce.dto.CommerceOrderResolutionRequestResponse;
import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.application.commerce.dto.CommerceAdminPaymentAttemptResponse;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;
import com.backend.shared.validation.Uid;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/commerce/admin")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Validated
public class CommerceAdminOrderController {

	private final CommerceAdminOrderService adminOrderService;
	private final CommerceOrderResolutionRequestService resolutionRequestService;
	private final MessageSource messageSource;

	@Autowired
	public CommerceAdminOrderController(
			CommerceAdminOrderService adminOrderService,
			CommerceOrderResolutionRequestService resolutionRequestService,
			MessageSource messageSource) {
		this.adminOrderService = adminOrderService;
		this.resolutionRequestService = resolutionRequestService;
		this.messageSource = messageSource;
	}

	CommerceAdminOrderController(CommerceAdminOrderService adminOrderService, MessageSource messageSource) {
		this(adminOrderService, null, messageSource);
	}

	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<CommerceAdminDashboardResponse>> dashboard() {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.dashboard.retrieved"),
				adminOrderService.dashboard()));
	}

	@GetMapping("/orders")
	public ResponseEntity<ApiResponse<PageableResponse<CommerceAdminOrderSummaryResponse>>> listOrders(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) @Size(max = 200) String search,
			@RequestParam(required = false) CommerceOrderStatus status,
			@RequestParam(required = false) Boolean requiresAttention) {
		String effectiveSort = SortParseUtil.getEffectiveSortCode(
				sort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_DEFAULT_SORT);
		Sort sortObj = SortParseUtil.parse(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_ALLOWED_FIELDS,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_DEFAULT_SORT);
		Page<CommerceAdminOrderSummaryResponse> orders = adminOrderService.listOrders(
				PageRequest.of(page, size, sortObj),
				search,
				status,
				requiresAttention);
		SortConfig sortConfig = SortConfig.of(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_SORT_OPTIONS);
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.orders.retrieved"),
				PageableResponse.from(orders, sortConfig)));
	}

	@GetMapping("/orders/{orderUid}")
	public ResponseEntity<ApiResponse<CommerceAdminOrderDetailResponse>> getOrder(
			@PathVariable @Uid String orderUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.order.retrieved"),
				adminOrderService.getOrder(orderUid)));
	}

	@PatchMapping("/orders/{orderUid}/status")
	public ResponseEntity<ApiResponse<CommerceAdminOrderDetailResponse>> changeOrderStatus(
			@PathVariable @Uid String orderUid,
			@Valid @RequestBody ChangeCommerceOrderStatusRequest request) {
		CommerceAdminOrderDetailResponse response = adminOrderService.changeStatus(
				orderUid,
				new ChangeCommerceOrderStatusCommand(
						request.status(),
						request.carrierName(),
						request.trackingNumber(),
						request.trackingUrl(),
						request.internalNote()));
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.order.status.updated"),
				response));
	}

	@GetMapping("/payment-attempts")
	public ResponseEntity<ApiResponse<PageableResponse<CommerceAdminPaymentAttemptResponse>>> listPaymentAttempts(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) @Size(max = 200) String search,
			@RequestParam(required = false) CommercePaymentAttemptStatus status) {
		String effectiveSort = SortParseUtil.getEffectiveSortCode(
				sort,
				SortableFieldsConfig.COMMERCE_ADMIN_PAYMENT_ATTEMPT_DEFAULT_SORT);
		Sort sortObj = SortParseUtil.parse(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_PAYMENT_ATTEMPT_ALLOWED_FIELDS,
				SortableFieldsConfig.COMMERCE_ADMIN_PAYMENT_ATTEMPT_DEFAULT_SORT);
		Page<CommerceAdminPaymentAttemptResponse> attempts = adminOrderService.listPaymentAttempts(
				PageRequest.of(page, size, sortObj),
				search,
				status);
		SortConfig sortConfig = SortConfig.of(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_PAYMENT_ATTEMPT_SORT_OPTIONS);
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.payment.attempts.retrieved"),
				PageableResponse.from(attempts, sortConfig)));
	}

	@GetMapping("/order-requests")
	public ResponseEntity<ApiResponse<PageableResponse<CommerceOrderResolutionRequestResponse>>> listOrderRequests(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) @Size(max = 200) String search,
			@RequestParam(required = false) CommerceOrderResolutionRequestType type,
			@RequestParam(required = false) CommerceOrderResolutionRequestStatus status) {
		String effectiveSort = SortParseUtil.getEffectiveSortCode(
				sort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_REQUEST_DEFAULT_SORT);
		Sort sortObj = SortParseUtil.parse(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_REQUEST_ALLOWED_FIELDS,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_REQUEST_DEFAULT_SORT);
		Page<CommerceOrderResolutionRequestResponse> requests = resolutionRequestService.listAdminRequests(
				PageRequest.of(page, size, sortObj),
				search,
				type,
				status);
		SortConfig sortConfig = SortConfig.of(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ADMIN_ORDER_REQUEST_SORT_OPTIONS);
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.order.requests.retrieved"),
				PageableResponse.from(requests, sortConfig)));
	}

	@GetMapping("/order-requests/{requestUid}")
	public ResponseEntity<ApiResponse<CommerceOrderResolutionRequestResponse>> getOrderRequest(
			@PathVariable @Uid String requestUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.order.request.retrieved"),
				resolutionRequestService.getAdminRequest(requestUid)));
	}

	@PatchMapping("/order-requests/{requestUid}/decision")
	public ResponseEntity<ApiResponse<CommerceOrderResolutionRequestResponse>> decideOrderRequest(
			@PathVariable @Uid String requestUid,
			@Valid @RequestBody CommerceOrderResolutionDecisionRequest request) {
		CommerceOrderResolutionRequestResponse response = resolutionRequestService.decide(
				requestUid,
				new CommerceOrderResolutionDecisionCommand(
						request.decision() == CommerceOrderResolutionDecisionRequest.Decision.APPROVE,
						request.decisionNote()));
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.admin.order.request.decided"),
				response));
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
