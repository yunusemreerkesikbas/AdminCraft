package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.MailOutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_mail_outbox", schema = "platform_management")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformMailOutbox extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private PlatformMailCampaign campaign;

    @Column(name = "to_email", nullable = false, length = 255)
    private String toEmail;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private MailOutboxStatus status = MailOutboxStatus.PENDING;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
