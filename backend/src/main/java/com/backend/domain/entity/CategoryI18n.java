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
@Table(name = "category_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uuid"}, name = "uk_category_i18n_uuid"),
        @UniqueConstraint(columnNames = {"uid"}, name = "uk_category_i18n_uid"),
        @UniqueConstraint(columnNames = {"category_id", "language"}, name = "uk_category_i18n_lang")
}, indexes = {
        @Index(columnList = "category_id", name = "idx_category_i18n_category"),
        @Index(columnList = "language", name = "idx_category_i18n_language")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"category"})
@NoArgsConstructor
@AllArgsConstructor
public class CategoryI18n extends BaseI18nEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
