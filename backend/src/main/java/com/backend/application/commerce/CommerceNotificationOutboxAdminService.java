package com.backend.application.commerce;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.commerce.dto.CommerceNotificationOutboxResponse;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationStatus;

public interface CommerceNotificationOutboxAdminService {

	Page<CommerceNotificationOutboxResponse> listOutbox(
			Pageable pageable,
			String search,
			CommerceNotificationStatus status,
			CommerceNotificationEventType eventType,
			String aggregateUid);

	CommerceNotificationOutboxResponse getOutbox(String outboxUid);

	CommerceNotificationOutboxResponse retry(String outboxUid);

	int retryDueNotificationsForCurrentTenant();
}
