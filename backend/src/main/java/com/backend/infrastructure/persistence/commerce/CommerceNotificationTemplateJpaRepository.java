package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;

interface CommerceNotificationTemplateJpaRepository extends JpaRepository<CommerceNotificationTemplate, Long> {

	Optional<CommerceNotificationTemplate> findFirstByTemplateKeyAndChannelAndLanguageAndActiveTrue(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);
}
