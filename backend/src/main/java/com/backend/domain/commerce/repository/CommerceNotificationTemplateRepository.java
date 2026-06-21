package com.backend.domain.commerce.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;

public interface CommerceNotificationTemplateRepository {

	List<CommerceNotificationTemplate> findAll(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language,
			Boolean active);

	Optional<CommerceNotificationTemplate> findByUid(String uid);

	Optional<CommerceNotificationTemplate> findExact(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);

	Optional<CommerceNotificationTemplate> findActive(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);

	CommerceNotificationTemplate save(CommerceNotificationTemplate template);
}
