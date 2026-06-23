package com.backend.infrastructure.sms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.backend.domain.port.SmsSenderPort;
import com.backend.domain.sms.SmsResult;

@Component
@Profile("!dev & !local & !test")
@ConditionalOnMissingBean(SmsSenderPort.class)
public class DisabledSmsSender implements SmsSenderPort {

	@Override
	public SmsResult send(String toPhone, String content) {
		return SmsResult.failure("commerce.sms.provider.disabled");
	}
}
