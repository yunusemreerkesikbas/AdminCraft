package com.backend.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.backend.domain.enums.ProductFieldType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a product field value.
 * Stores the actual value for a specific product and field definition.
 * Uses polymorphic columns to support different data types.
 */
@Entity
@Table(name = "product_field_values", uniqueConstraints = @UniqueConstraint(name = "uk_pfv_product_field", columnNames = {
    "product_id", "field_definition_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductFieldValue extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "field_definition_id", nullable = false)
  private ProductFieldDefinition fieldDefinition;

  @Column(name = "value_text", columnDefinition = "TEXT")
  private String valueText;

  @Column(name = "value_number", precision = 19, scale = 4)
  private BigDecimal valueNumber;

  @Column(name = "value_boolean")
  private Boolean valueBoolean;

  @Column(name = "value_date")
  private LocalDate valueDate;

  /**
   * Gets the value based on the field type.
   * 
   * @return The value as Object
   */
  public Object getValue() {
    if (valueText != null)
      return valueText;
    if (valueNumber != null)
      return valueNumber;
    if (valueBoolean != null)
      return valueBoolean;
    if (valueDate != null)
      return valueDate;
    return null;
  }

  /**
   * Sets the value based on the object type.
   * Validates that the value type matches the expected field type.
   * Performs automatic type conversion for String values when field type is
   * known.
   *
   * @param value The value to set
   * @throws IllegalArgumentException if value type doesn't match field definition
   *                                  type
   */
  public void setValue(Object value) {
    // Reset all values first
    this.valueText = null;
    this.valueNumber = null;
    this.valueBoolean = null;
    this.valueDate = null;

    if (value == null)
      return;

    // Validate type if fieldDefinition is set
    if (fieldDefinition != null && fieldDefinition.getFieldType() != null) {
      validateValueType(value, fieldDefinition.getFieldType());
    }

    // Handle String values with type conversion based on field definition
    if (value instanceof String str && fieldDefinition != null && fieldDefinition.getFieldType() != null) {
      ProductFieldType type = fieldDefinition.getFieldType();
      switch (type) {
        case NUMBER:
          if (canConvertToNumber(str)) {
            this.valueNumber = new BigDecimal(str);
            return;
          }
          break;
        case BOOLEAN:
          if (canConvertToBoolean(str)) {
            this.valueBoolean = Boolean.parseBoolean(str);
            return;
          }
          break;
        case DATE:
          if (canConvertToDate(str)) {
            this.valueDate = LocalDate.parse(str);
            return;
          }
          break;
        default:
          // TEXT, RICHTEXT, MEDIA - keep as string
          break;
      }
      this.valueText = str;
    } else if (value instanceof String str) {
      this.valueText = str;
    } else if (value instanceof Number num) {
      this.valueNumber = new BigDecimal(num.toString());
    } else if (value instanceof Boolean bool) {
      this.valueBoolean = bool;
    } else if (value instanceof LocalDate date) {
      this.valueDate = date;
    } else {
      // Fallback to string representation
      this.valueText = value.toString();
    }
  }

  /**
   * Validates that the value type matches the expected field type.
   *
   * @param value     The value to validate
   * @param fieldType The expected field type
   * @throws IllegalArgumentException if types don't match
   */
  private void validateValueType(Object value, ProductFieldType fieldType) {
    boolean isValid = switch (fieldType) {
      case TEXT, RICHTEXT, MEDIA -> value instanceof String || canConvertToString(value);
      case NUMBER -> value instanceof Number || canConvertToNumber(value);
      case BOOLEAN -> value instanceof Boolean || canConvertToBoolean(value);
      case DATE -> value instanceof LocalDate || canConvertToDate(value);
    };

    if (!isValid) {
      throw new IllegalArgumentException(
          String.format("Value type '%s' is not compatible with field type '%s'",
              value.getClass().getSimpleName(), fieldType));
    }
  }

  private boolean canConvertToString(Object value) {
    return value != null; // Everything can be converted to string
  }

  private boolean canConvertToNumber(Object value) {
    if (value instanceof String str) {
      try {
        new BigDecimal(str);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  private boolean canConvertToBoolean(Object value) {
    if (value instanceof String str) {
      return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
    return false;
  }

  private boolean canConvertToDate(Object value) {
    if (value instanceof String str) {
      try {
        LocalDate.parse(str);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }
}
