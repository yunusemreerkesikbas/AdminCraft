package com.backend.domain.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_category_uid"),
        @UniqueConstraint(columnNames = { "code" }, name = "uk_category_code")
}, indexes = {
        @Index(columnList = "parent_id", name = "idx_category_parent"),
        @Index(columnList = "sort_order", name = "idx_category_sort"),
        @Index(columnList = "is_visible", name = "idx_category_visible")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "parent", "children", "i18nContent" })
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "parent_id")
    private Long parentId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @ToString.Exclude
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private Set<Category> children = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CategoryI18n> i18nContent = new HashSet<>();

    public boolean isRoot() {
        return parentId == null;
    }

    public void addChild(Category child) {
        children.add(child);
        child.setParentId(this.getId());
    }

    public void addI18n(CategoryI18n i18n) {
        i18nContent.add(i18n);
        i18n.setCategory(this);
    }
}
