package com.backend.domain.entity;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.UnauthorizedOperationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pages", indexes = {
        @Index(columnList = "tenant_id", name = "idx_page_tenant"),
        @Index(columnList = "status", name = "idx_page_status"),
        @Index(columnList = "category_id", name = "idx_page_category"),
        @Index(columnList = "tenant_id, is_home", name = "idx_page_home"),
        @Index(columnList = "sort_order", name = "idx_page_sort")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Page extends BaseEntity {

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PageStatus status = PageStatus.DRAFT;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @Column(name = "style_classes", length = 255)
    private String styleClasses;

    @Column(name = "is_home")
    private Boolean isHome = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public boolean canBePublished() {
        return status == PageStatus.DRAFT;
    }

    public void markAsHome() {
        this.isHome = true;
    }

    public void unmarkAsHome() {
        this.isHome = false;
    }

    public boolean canBeEditedBy(Long userId, Long userTenantId) {
        return getTenantId() != null && getTenantId().equals(userTenantId);
    }

    public void validateEditAuthorization(Long userId, Long userTenantId) {
        if (!canBeEditedBy(userId, userTenantId)) {
            throw new UnauthorizedOperationException(
                    "User " + userId + " cannot edit page from different tenant");
        }
    }

    public boolean isPublished() {
        return status == PageStatus.PUBLISHED;
    }

    public boolean isScheduled() {
        return status == PageStatus.SCHEDULED;
    }

    public boolean isArchived() {
        return status == PageStatus.ARCHIVED;
    }

    public boolean isHomePage() {
        return Boolean.TRUE.equals(isHome);
    }
}
