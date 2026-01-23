package com.backend.domain.entity;

import com.backend.domain.enums.ProductFieldType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a global product field definition.
 * These fields are visible across ALL products regardless of ProductType.
 */
@Entity
@Table(name = "product_field_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductFieldDefinition extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "field_type", nullable = false, length = 20)
  private ProductFieldType fieldType;

  @Column(name = "is_required", nullable = false)
  @Builder.Default
  private Boolean isRequired = false;

  @Column(name = "is_visible_in_list", nullable = false)
  @Builder.Default
  private Boolean isVisibleInList = true;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private Integer sortOrder = 0;

  @Column(name = "default_value", columnDefinition = "TEXT")
  private String defaultValue;

  @Column(name = "validation_config", columnDefinition = "JSON")
  private String validationConfig;
}
