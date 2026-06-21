package com.backend.infrastructure.sms;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.application.dto.sms.SmsResult;
import com.backend.domain.port.SmsSenderPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleSmsSender implements SmsSenderPort {

	@Override
	public SmsResult send(String toPhone, String content) {
		String messageId = "console-sms-" + UUID.nameUUIDFromBytes(
				(toPhone + "|" + content).getBytes(StandardCharsets.UTF_8));
		log.info("Console SMS queued to={} messageId={}", maskPhone(toPhone), messageId);
		return SmsResult.success(messageId);
	}

	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 4) {
			return "***";
		}
		return "***" + phone.substring(phone.length() - 4);
	}
}
