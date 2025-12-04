package com.backend.domain.entity;

import com.backend.domain.enums.PageStatus;

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
        @Index(columnList = "category_id", name = "idx_page_category"),
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

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public void validateEditAuthorization(Long userId, Long userTenantId) {
    }
}
