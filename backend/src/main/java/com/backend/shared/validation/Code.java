package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_CODE_PATTERN;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CodeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Code {

  String message() default MSG_CODE_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean required() default true;

  int maxLength() default 50;
}
