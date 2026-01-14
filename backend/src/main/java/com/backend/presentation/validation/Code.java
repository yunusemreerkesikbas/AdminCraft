package com.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.backend.shared.constants.ValidationConstants.MSG_CODE_PATTERN;

/**
 * Validates that a field contains a valid code format.
 * Code must start with lowercase letter and contain only lowercase letters, digits, and underscores.
 *
 * <p>Use this annotation for Product Type codes, Attribute codes, and similar identifiers.</p>
 *
 * <p>Pattern: ^[a-z][a-z0-9_]*$</p>
 *
 * <p>Examples of valid codes: "product_type", "color", "size_xl", "a1"</p>
 * <p>Examples of invalid codes: "1product", "Product", "code-name", ""</p>
 *
 * @see CodeValidator
 */
@Documented
@Constraint(validatedBy = CodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Code {

    String message() default MSG_CODE_PATTERN;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether the field is required (not null/empty).
     * Default is true.
     */
    boolean required() default true;

    /**
     * Maximum length allowed.
     * Default is 50 (from ValidationConstants.CODE_MAX_LENGTH).
     */
    int maxLength() default 50;
}
