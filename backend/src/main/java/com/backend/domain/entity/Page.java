package com.backend.domain.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.PageType;
import com.backend.domain.enums.RobotTag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pages", indexes = {
        @Index(columnList = "status", name = "idx_page_status"),
        @Index(columnList = "template_id", name = "idx_page_template")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Page extends BaseEntity {

    @Column(name = "template_id")
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PageStatus status = PageStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", length = 20)
    private PageType pageType = PageType.CONTENT;

    @Column(name = "is_home")
    private Boolean isHome = false;

    @Column(name = "style_classes", length = 255)
    private String styleClasses;

    @Enumerated(EnumType.STRING)
    @Column(name = "robot_tag", length = 50)
    private RobotTag robotTag = RobotTag.INDEX_FOLLOW;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    public void validateEditAuthorization(Long userId, Long userTenantId) {
    }
}
