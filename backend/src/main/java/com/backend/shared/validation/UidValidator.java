package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_UID_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_UID_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_UID_SIZE;
import static com.backend.shared.constants.ValidationConstants.UID_PATTERN;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UidValidator implements ConstraintValidator<Uid, String> {

  private static final Pattern UID_REGEX = Pattern.compile(UID_PATTERN);

  private Uid annotation;

  @Override
  public void initialize(Uid constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_UID_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() > maxLength) {
      setCustomMessage(context, MSG_UID_SIZE);
      return false;
    }

    if (!UID_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_UID_PATTERN);
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
