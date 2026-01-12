package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_category_links", indexes = {
        @Index(columnList = "category_id", name = "idx_pcl_category"),
        @Index(columnList = "is_primary", name = "idx_pcl_primary")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryLink {

    @EmbeddedId
    private ProductCategoryLinkId id = new ProductCategoryLinkId();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public ProductCategoryLink(Product product, Category category, Boolean isPrimary) {
        this.product = product;
        this.category = category;
        this.isPrimary = isPrimary;
        this.id = new ProductCategoryLinkId(product.getId(), category.getId());
    }
}
