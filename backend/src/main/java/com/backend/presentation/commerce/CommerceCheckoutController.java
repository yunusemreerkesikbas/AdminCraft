package com.backend.presentation.commerce;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CheckoutService;
import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.dto.CheckoutAddressSelectionCommand;
import com.backend.application.commerce.dto.CheckoutResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.validation.Uid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/checkout")
@RequiredArgsConstructor
@Tag(name = "Commerce Checkout", description = "Customer checkout foundation API")
public class CommerceCheckoutController {

	private final CheckoutService checkoutService;
	private final MessageSource messageSource;

	@PostMapping
	@Operation(summary = "Start customer checkout")
	public ResponseEntity<ApiResponse<CheckoutResponse>> start(
			Authentication authentication,
			@Valid @RequestBody(required = false) CheckoutAddressSelectionRequest request) {
		CheckoutResponse response = checkoutService.start(principal(authentication), toCommand(request));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.checkout.started"), response));
	}

	@GetMapping("/current")
	@Operation(summary = "Get current customer checkout")
	public ResponseEntity<ApiResponse<CheckoutResponse>> current(Authentication authentication) {
		CheckoutResponse response = checkoutService.getCurrent(principal(authentication));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.checkout.retrieved"), response));
	}

	@PatchMapping("/{checkoutUid}/addresses")
	@Operation(summary = "Update checkout addresses")
	public ResponseEntity<ApiResponse<CheckoutResponse>> updateAddresses(
			Authentication authentication,
			@PathVariable @Uid String checkoutUid,
			@Valid @RequestBody CheckoutAddressSelectionRequest request) {
		CheckoutResponse response = checkoutService.updateAddresses(principal(authentication), checkoutUid, toCommand(request));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.checkout.addresses.updated"), response));
	}

	private CheckoutAddressSelectionCommand toCommand(CheckoutAddressSelectionRequest request) {
		if (request == null) {
			return new CheckoutAddressSelectionCommand(null, null, null);
		}
		return new CheckoutAddressSelectionCommand(
				request.deliveryAddressUid(),
				request.billingAddressUid(),
				request.billingSameAsDelivery());
	}

	private CommerceCustomerPrincipal principal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CommerceCustomerPrincipal principal)) {
			throw new IllegalArgumentException("commerce.customer.auth.required");
		}
		return principal;
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
