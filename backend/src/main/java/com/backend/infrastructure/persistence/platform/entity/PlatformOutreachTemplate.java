package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "platform_outreach_templates", schema = "platform_management")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PlatformOutreachTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "TR";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
