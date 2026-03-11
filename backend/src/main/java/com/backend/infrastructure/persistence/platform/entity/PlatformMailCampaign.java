package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.MailCampaignStatus;

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
@Table(name = "platform_mail_campaigns", schema = "platform_management")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformMailCampaign extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "template_id")
    private PlatformEmailTemplate template;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private MailCampaignStatus status = MailCampaignStatus.DRAFT;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "sent_count", nullable = false)
    private Integer sentCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    // Separate from BaseEntity.createdBy (Long user ID): stores the email/name of the platform admin who created the campaign
    @Column(name = "created_by_email", length = 100)
    private String createdByEmail;
}
