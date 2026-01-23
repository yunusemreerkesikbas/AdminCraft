package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_SLUG_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_SLUG_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_SLUG_SIZE;
import static com.backend.shared.constants.ValidationConstants.SLUG_PATTERN;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SlugValidator implements ConstraintValidator<Slug, String> {

  private static final Pattern SLUG_REGEX = Pattern.compile(SLUG_PATTERN);

  private Slug annotation;

  @Override
  public void initialize(Slug constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int minLength = annotation.minLength();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_SLUG_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() < minLength || value.length() > maxLength) {
      setCustomMessage(context, MSG_SLUG_SIZE);
      return false;
    }

    if (!SLUG_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_SLUG_PATTERN);
      return false;
    }

    return true;
  }

  private void setCustomMessage(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message)
        .addConstraintViolation();
  }
}
