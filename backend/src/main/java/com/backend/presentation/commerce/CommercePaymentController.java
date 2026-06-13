package com.backend.presentation.commerce;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.PaymentAttemptService;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.validation.Uid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/payments")
@RequiredArgsConstructor
@Tag(name = "Commerce Payments", description = "Customer payment attempt foundation API")
public class CommercePaymentController {

	private final PaymentAttemptService paymentAttemptService;
	private final MessageSource messageSource;

	@PostMapping("/attempts")
	@Operation(summary = "Create payment attempt")
	public ResponseEntity<ApiResponse<PaymentAttemptResponse>> createAttempt(
			Authentication authentication,
			@Valid @RequestBody CreatePaymentAttemptRequest request) {
		PaymentAttemptResponse response = paymentAttemptService.create(
				principal(authentication),
				new CreatePaymentAttemptCommand(request.checkoutUid()));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.payment.attempt.created"), response));
	}

	@GetMapping("/attempts/{attemptUid}")
	@Operation(summary = "Get payment attempt")
	public ResponseEntity<ApiResponse<PaymentAttemptResponse>> getAttempt(
			Authentication authentication,
			@PathVariable @Uid String attemptUid) {
		PaymentAttemptResponse response = paymentAttemptService.get(principal(authentication), attemptUid);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.payment.attempt.retrieved"), response));
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
