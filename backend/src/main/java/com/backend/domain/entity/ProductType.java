package com.backend.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uid"}, name = "uk_product_type_uid"),
        @UniqueConstraint(columnNames = {"code"}, name = "uk_product_type_code")
}, indexes = {
        @Index(columnList = "category", name = "idx_product_type_category")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"attributeDefinitions"})
@NoArgsConstructor
@AllArgsConstructor
public class ProductType extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    @ToString.Exclude
    @OneToMany(mappedBy = "productType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ProductAttributeDefinition> attributeDefinitions = new ArrayList<>();

    public void addAttributeDefinition(ProductAttributeDefinition definition) {
        attributeDefinitions.add(definition);
        definition.setProductType(this);
    }

    public void removeAttributeDefinition(ProductAttributeDefinition definition) {
        attributeDefinitions.remove(definition);
        definition.setProductType(null);
    }
}
