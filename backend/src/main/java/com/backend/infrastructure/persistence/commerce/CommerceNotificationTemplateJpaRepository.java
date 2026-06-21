package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;

interface CommerceNotificationTemplateJpaRepository extends JpaRepository<CommerceNotificationTemplate, Long> {

	Optional<CommerceNotificationTemplate> findByUid(String uid);

	Optional<CommerceNotificationTemplate> findFirstByTemplateKeyAndChannelAndLanguage(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);

	Optional<CommerceNotificationTemplate> findFirstByTemplateKeyAndChannelAndLanguageAndActiveTrue(
			CommerceNotificationEventType templateKey,
			CommerceNotificationChannel channel,
			String language);

	@Query("""
			select template from CommerceNotificationTemplate template
			where (:templateKey is null or template.templateKey = :templateKey)
			  and template.channel = :channel
			  and (:language is null or template.language = :language)
			  and (:active is null or template.active = :active)
			order by template.templateKey asc, template.language asc
			""")
	java.util.List<CommerceNotificationTemplate> findAdminTemplates(
			@Param("templateKey") CommerceNotificationEventType templateKey,
			@Param("channel") CommerceNotificationChannel channel,
			@Param("language") String language,
			@Param("active") Boolean active);
}
