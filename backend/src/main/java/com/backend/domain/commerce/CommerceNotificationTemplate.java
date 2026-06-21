package com.backend.domain.commerce;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "commerce_notification_templates", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_notification_template_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_notification_template_uid"),
		@UniqueConstraint(columnNames = { "template_key", "channel", "language" }, name = "uk_commerce_notification_template_key_channel_lang")
}, indexes = {
		@Index(columnList = "template_key, channel, language, is_active", name = "idx_commerce_notification_template_lookup")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommerceNotificationTemplate extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "template_key", nullable = false, length = 60)
	private CommerceNotificationEventType templateKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceNotificationChannel channel = CommerceNotificationChannel.EMAIL;

	@Column(nullable = false, length = 10)
	private String language;

	@Column(nullable = false, length = 255)
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_active", nullable = false)
	private Boolean active = true;
}
