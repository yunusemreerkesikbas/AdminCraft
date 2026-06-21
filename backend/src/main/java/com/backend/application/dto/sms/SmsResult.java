package com.backend.application.dto.sms;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsResult {

	private final boolean success;
	private final String messageId;
	private final String errorMessage;

	public static SmsResult success(String messageId) {
		return SmsResult.builder()
				.success(true)
				.messageId(messageId)
				.build();
	}

	public static SmsResult failure(String errorMessage) {
		return SmsResult.builder()
				.success(false)
				.errorMessage(errorMessage)
				.build();
	}
}
