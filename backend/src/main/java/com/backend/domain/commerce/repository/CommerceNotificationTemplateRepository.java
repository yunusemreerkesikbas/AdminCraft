package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;

public interface CommerceNotificationTemplateRepository {

	Optional<CommerceNotificationTemplate> findActive(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);
}
