package com.backend.application.commerce.dto;

import com.backend.shared.validation.Uid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InitializePaymentAttemptCommand(
		@NotBlank(message = "{commerce.payment.attempt.uid.required}") @Size(max = 64) @Uid String attemptUid,
		@NotBlank(message = "{commerce.payment.callback.url.required}") @Size(max = 2048) String callbackUrl,
		@NotBlank @Size(max = 45) String clientIp) {
}
