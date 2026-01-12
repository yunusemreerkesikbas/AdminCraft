package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uuid" }, name = "uk_product_i18n_uuid"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_product_i18n_uid"),
        @UniqueConstraint(columnNames = { "product_id", "language" }, name = "uk_product_i18n_lang")
}, indexes = {
        @Index(columnList = "product_id", name = "idx_product_i18n_product"),
        @Index(columnList = "language", name = "idx_product_i18n_language")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "product" })
@NoArgsConstructor
@AllArgsConstructor
public class ProductI18n extends BaseI18nEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 500)
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 200)
    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Size(max = 500)
    @Column(name = "seo_description", length = 500)
    private String seoDescription;
}
