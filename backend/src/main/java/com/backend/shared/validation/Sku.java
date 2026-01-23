package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_SKU_PATTERN;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a field contains a valid SKU (Stock Keeping Unit) format.
 * SKU can contain letters (uppercase and lowercase), digits, underscores, and
 * hyphens.
 *
 * <p>
 * Use this annotation for Product SKU fields.
 * </p>
 *
 * <p>
 * Pattern: ^[A-Za-z0-9_-]+$
 * </p>
 *
 * <p>
 * Examples of valid SKUs: "SKU-12345", "PROD_001", "ABC123", "item-1_v2"
 * </p>
 * <p>
 * Examples of invalid SKUs: "SKU 123", "item.name", "", "product@123"
 * </p>
 *
 * @see SkuValidator
 */
@Documented
@Constraint(validatedBy = SkuValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Sku {

  String message() default MSG_SKU_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * Whether the field is required (not null/empty).
   * Default is true.
   */
  boolean required() default true;

  /**
   * Maximum length allowed.
   * Default is 100 (from ValidationConstants.SKU_MAX_LENGTH).
   */
  int maxLength() default 100;
}
