package com.backend.domain.commerce;

import java.time.LocalDateTime;

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
@Table(name = "commerce_legal_templates", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_legal_template_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_legal_template_uid"),
		@UniqueConstraint(columnNames = { "template_type", "language", "version" }, name = "uk_commerce_legal_template_type_lang_version")
}, indexes = {
		@Index(columnList = "template_type, language, status", name = "idx_commerce_legal_template_type_lang_status"),
		@Index(columnList = "status, updated_at", name = "idx_commerce_legal_template_status_updated")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommerceLegalTemplate extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "template_type", nullable = false, length = 60)
	private CommerceLegalTemplateType type;

	@Column(nullable = false, length = 10)
	private String language;

	@Column(nullable = false)
	private Integer version = 1;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceLegalTemplateStatus status = CommerceLegalTemplateStatus.DRAFT;

	@Column(nullable = false, length = 191)
	private String title;

	@Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
	private String contentText;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;
}
