package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceNotificationTemplateRepositoryImpl implements CommerceNotificationTemplateRepository {

	private final CommerceNotificationTemplateJpaRepository jpaRepository;

	@Override
	public Optional<CommerceNotificationTemplate> findActive(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language) {
		return jpaRepository.findFirstByTemplateKeyAndChannelAndLanguageAndActiveTrue(templateKey, channel, language);
	}
}
