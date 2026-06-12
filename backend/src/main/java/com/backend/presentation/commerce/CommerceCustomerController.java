package com.backend.presentation.commerce;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceCustomerAddressService;
import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommerceCustomerProfileService;
import com.backend.application.commerce.dto.CommerceCustomerAddressCommand;
import com.backend.application.commerce.dto.UpdateCommerceCustomerProfileCommand;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/customers")
@RequiredArgsConstructor
public class CommerceCustomerController {

	private final CommerceCustomerProfileService profileService;
	private final CommerceCustomerAddressService addressService;
	private final MessageSource messageSource;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<?>> me(Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.profile.retrieved"), profileService.getMe(principal(authentication))));
	}

	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<?>> updateMe(
			Authentication authentication,
			@Valid @RequestBody UpdateCommerceCustomerProfileRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.customer.profile.updated"),
				profileService.updateMe(principal(authentication), new UpdateCommerceCustomerProfileCommand(
						request.firstName(),
						request.lastName(),
						request.phone(),
						request.gender(),
						request.birthDate()))));
	}

	@GetMapping("/addresses")
	public ResponseEntity<ApiResponse<?>> listAddresses(Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.address.list.success"), addressService.list(principal(authentication))));
	}

	@PostMapping("/addresses")
	public ResponseEntity<ApiResponse<?>> createAddress(
			Authentication authentication,
			@Valid @RequestBody CommerceCustomerAddressRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.customer.address.created"),
				addressService.create(principal(authentication), toCommand(request))));
	}

	@PatchMapping("/addresses/{addressUid}")
	public ResponseEntity<ApiResponse<?>> updateAddress(
			Authentication authentication,
			@PathVariable String addressUid,
			@Valid @RequestBody CommerceCustomerAddressRequest request) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.customer.address.updated"),
				addressService.update(principal(authentication), addressUid, toCommand(request))));
	}

	@DeleteMapping("/addresses/{addressUid}")
	public ResponseEntity<ApiResponse<Void>> deleteAddress(
			Authentication authentication,
			@PathVariable String addressUid) {
		addressService.delete(principal(authentication), addressUid);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.address.deleted"), null));
	}

	@PostMapping("/addresses/{addressUid}/default-delivery")
	public ResponseEntity<ApiResponse<?>> setDefaultDelivery(
			Authentication authentication,
			@PathVariable String addressUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.customer.address.default.delivery.updated"),
				addressService.setDefaultDelivery(principal(authentication), addressUid)));
	}

	@PostMapping("/addresses/{addressUid}/default-billing")
	public ResponseEntity<ApiResponse<?>> setDefaultBilling(
			Authentication authentication,
			@PathVariable String addressUid) {
		return ResponseEntity.ok(ApiResponse.success(
				message("commerce.customer.address.default.billing.updated"),
				addressService.setDefaultBilling(principal(authentication), addressUid)));
	}

	private CommerceCustomerAddressCommand toCommand(CommerceCustomerAddressRequest request) {
		return new CommerceCustomerAddressCommand(
				request.label(),
				request.firstName(),
				request.lastName(),
				request.phone(),
				request.countryIso(),
				request.city(),
				request.district(),
				request.addressLine1(),
				request.addressLine2(),
				request.postalCode(),
				request.defaultDelivery(),
				request.defaultBilling(),
				request.invoiceType(),
				request.companyName(),
				request.taxNumber(),
				request.taxOffice(),
				request.invoiceIdentityNumber());
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
