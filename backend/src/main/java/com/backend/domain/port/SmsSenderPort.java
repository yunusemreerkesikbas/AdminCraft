package com.backend.domain.port;

import com.backend.domain.sms.SmsResult;

public interface SmsSenderPort {

	SmsResult send(String toPhone, String content);
}
