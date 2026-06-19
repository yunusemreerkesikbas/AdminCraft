package com.backend.presentation.commerce;

import java.util.Locale;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommerceOrderReadService;
import com.backend.application.commerce.CommerceOrderResolutionRequestService;
import com.backend.application.commerce.dto.CreateCommerceOrderResolutionRequestCommand;
import com.backend.application.commerce.dto.CustomerOrderResolutionRequestResponse;
import com.backend.application.commerce.dto.CommerceOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceOrderSummaryResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;
import com.backend.shared.validation.Uid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/commerce/orders")
@Validated
@Tag(name = "Commerce Orders", description = "Customer order read API")
public class CommerceOrderController {

	private final CommerceOrderReadService orderReadService;
	private final CommerceOrderResolutionRequestService resolutionRequestService;
	private final MessageSource messageSource;

	@Autowired
	public CommerceOrderController(
			CommerceOrderReadService orderReadService,
			CommerceOrderResolutionRequestService resolutionRequestService,
			MessageSource messageSource) {
		this.orderReadService = orderReadService;
		this.resolutionRequestService = resolutionRequestService;
		this.messageSource = messageSource;
	}

	CommerceOrderController(CommerceOrderReadService orderReadService, MessageSource messageSource) {
		this(orderReadService, null, messageSource);
	}

	@GetMapping
	@Operation(summary = "List customer orders")
	public ResponseEntity<ApiResponse<PageableResponse<CommerceOrderSummaryResponse>>> list(
			Authentication authentication,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(required = false) String sort) {
		String effectiveSort = SortParseUtil.getEffectiveSortCode(sort, SortableFieldsConfig.COMMERCE_ORDER_DEFAULT_SORT);
		Sort sortObj = SortParseUtil.parse(
				effectiveSort,
				SortableFieldsConfig.COMMERCE_ORDER_ALLOWED_FIELDS,
				SortableFieldsConfig.COMMERCE_ORDER_DEFAULT_SORT);
		Page<CommerceOrderSummaryResponse> orders = orderReadService.list(
				principal(authentication),
				PageRequest.of(page, size, sortObj));
		SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.COMMERCE_ORDER_SORT_OPTIONS);
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.order.list.retrieved"),
				PageableResponse.from(orders, sortConfig)));
	}

	@GetMapping("/{orderUid}")
	@Operation(summary = "Get customer order detail")
	public ResponseEntity<ApiResponse<CommerceOrderDetailResponse>> get(
			Authentication authentication,
			@PathVariable @Uid String orderUid) {
		CommerceOrderDetailResponse response = orderReadService.get(principal(authentication), orderUid);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.order.retrieved"), response));
	}

	@PostMapping("/{orderUid}/requests")
	@Operation(summary = "Create customer order cancellation or return request")
	public ResponseEntity<ApiResponse<CustomerOrderResolutionRequestResponse>> createRequest(
			Authentication authentication,
			@PathVariable @Uid String orderUid,
			@Valid @RequestBody CreateCommerceOrderResolutionRequest request) {
		CustomerOrderResolutionRequestResponse response = resolutionRequestService.createCustomerRequest(
				principal(authentication),
				orderUid,
				new CreateCommerceOrderResolutionRequestCommand(
						request.requestType(),
						request.reason(),
						request.description()));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.order.request.created"), response));
	}

	@GetMapping("/{orderUid}/requests")
	@Operation(summary = "List customer order cancellation and return requests")
	public ResponseEntity<ApiResponse<List<CustomerOrderResolutionRequestResponse>>> listRequests(
			Authentication authentication,
			@PathVariable @Uid String orderUid) {
		List<CustomerOrderResolutionRequestResponse> response = resolutionRequestService.listCustomerRequests(
				principal(authentication),
				orderUid);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.order.requests.retrieved"), response));
	}

	private CommerceCustomerPrincipal principal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CommerceCustomerPrincipal principal)) {
			throw new AccessDeniedException("commerce.customer.auth.required");
		}
		return principal;
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
