package com.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.backend.shared.constants.ValidationConstants.MSG_SLUG_PATTERN;

@Documented
@Constraint(validatedBy = SlugValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug {

    String message() default MSG_SLUG_PATTERN;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean required() default true;

    int minLength() default 3;

    int maxLength() default 200;
}
