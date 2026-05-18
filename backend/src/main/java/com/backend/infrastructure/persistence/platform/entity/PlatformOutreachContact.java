package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.OutreachContactStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_outreach_contacts", schema = "platform_management")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformOutreachContact extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private OutreachContactStatus status = OutreachContactStatus.ACTIVE;
}
