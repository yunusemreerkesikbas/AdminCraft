package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_CODE_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_CODE_SIZE;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CodeValidator implements ConstraintValidator<Code, String> {

  private static final Pattern CODE_REGEX = Pattern.compile(CODE_PATTERN);

  private Code annotation;

  @Override
  public void initialize(Code constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_CODE_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() > maxLength) {
      setCustomMessage(context, MSG_CODE_SIZE);
      return false;
    }

    if (!CODE_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_CODE_PATTERN);
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
