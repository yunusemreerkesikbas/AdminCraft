package com.backend.domain.port;

import com.backend.application.dto.sms.SmsResult;

public interface SmsSenderPort {

	SmsResult send(String toPhone, String content);
}
