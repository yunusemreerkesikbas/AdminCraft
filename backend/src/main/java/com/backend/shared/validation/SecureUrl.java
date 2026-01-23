package com.backend.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom validation annotation for secure URL validation
 * Validates URLs against whitelisted domains to prevent URL injection attacks
 */
@Documented
@Constraint(validatedBy = SecureUrlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureUrl {

  String message() default "validation.url.security";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  SecureUrlType type() default SecureUrlType.GENERAL;
}
