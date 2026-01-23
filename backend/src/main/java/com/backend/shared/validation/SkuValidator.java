package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.MSG_SKU_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_SKU_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_SKU_SIZE;
import static com.backend.shared.constants.ValidationConstants.SKU_PATTERN;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SkuValidator implements ConstraintValidator<Sku, String> {

  private static final Pattern SKU_REGEX = Pattern.compile(SKU_PATTERN);

  private Sku annotation;

  @Override
  public void initialize(Sku constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_SKU_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() > maxLength) {
      setCustomMessage(context, MSG_SKU_SIZE);
      return false;
    }

    if (!SKU_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_SKU_PATTERN);
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
