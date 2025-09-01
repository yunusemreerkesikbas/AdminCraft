package com.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for secure URL validation
 * Validates URLs against whitelisted domains to prevent URL injection attacks
 */
@Documented
@Constraint(validatedBy = SecureUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureUrl {
    
    String message() default "validation.url.security";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    SecureUrlType type() default SecureUrlType.GENERAL;
}