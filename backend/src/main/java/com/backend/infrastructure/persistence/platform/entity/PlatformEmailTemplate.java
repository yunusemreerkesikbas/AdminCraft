package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_email_templates", schema = "platform_management", uniqueConstraints = {
    @UniqueConstraint(name = "uk_platform_email_template_key_lang", columnNames = { "template_key", "language" })
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformEmailTemplate extends BaseEntity {

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
