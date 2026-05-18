package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.OutreachCampaignStatus;

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
@Table(name = "platform_outreach_campaigns", schema = "platform_management")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformOutreachCampaign extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private PlatformOutreachTemplate template;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private OutreachCampaignStatus status = OutreachCampaignStatus.DRAFT;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "sent_count", nullable = false)
    private Integer sentCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    @Column(name = "created_by_email", length = 255)
    private String createdByEmail;
}
