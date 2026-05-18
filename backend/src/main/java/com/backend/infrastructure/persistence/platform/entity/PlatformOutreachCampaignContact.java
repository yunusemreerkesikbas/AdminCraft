package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.OutreachCampaignContactStatus;

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
@Table(name = "platform_outreach_campaign_contacts", schema = "platform_management")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformOutreachCampaignContact extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private PlatformOutreachCampaign campaign;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private PlatformOutreachContact contact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private OutreachCampaignContactStatus status = OutreachCampaignContactStatus.PENDING;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "rendered_subject", length = 500)
    private String renderedSubject;

    @Column(name = "rendered_content", columnDefinition = "MEDIUMTEXT")
    private String renderedContent;
}
