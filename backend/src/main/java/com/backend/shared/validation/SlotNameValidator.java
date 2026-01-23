package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_SLOT_NAME_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_SLOT_NAME_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_SLOT_NAME_SIZE;
import static com.backend.shared.constants.ValidationConstants.SLOT_NAME_PATTERN;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SlotNameValidator implements ConstraintValidator<SlotName, String> {

  private static final Pattern SLOT_NAME_REGEX = Pattern.compile(SLOT_NAME_PATTERN);

  private SlotName annotation;

  @Override
  public void initialize(SlotName constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_SLOT_NAME_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() > maxLength) {
      setCustomMessage(context, MSG_SLOT_NAME_SIZE);
      return false;
    }

    if (!SLOT_NAME_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_SLOT_NAME_PATTERN);
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
