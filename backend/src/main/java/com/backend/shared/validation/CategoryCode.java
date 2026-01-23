package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_CATEGORY_CODE_PATTERN;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a field contains a valid category code format.
 * Category code must start with lowercase letter and contain only lowercase
 * letters,
 * digits, underscores, and hyphens.
 *
 * <p>
 * Use this annotation for Category codes where hyphens are allowed.
 * </p>
 *
 * <p>
 * Pattern: ^[a-z][a-z0-9_-]*$
 * </p>
 *
 * <p>
 * Examples of valid codes: "electronics-main", "clothing_men", "home-decor",
 * "a1"
 * </p>
 * <p>
 * Examples of invalid codes: "1category", "Category", "code.name", ""
 * </p>
 *
 * @see CategoryCodeValidator
 */
@Documented
@Constraint(validatedBy = CategoryCodeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CategoryCode {

  String message() default MSG_CATEGORY_CODE_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * Whether the field is required (not null/empty).
   * Default is true.
   */
  boolean required() default true;

  /**
   * Maximum length allowed.
   * Default is 50 (from ValidationConstants.CATEGORY_CODE_MAX_LENGTH).
   */
  int maxLength() default 50;
}
