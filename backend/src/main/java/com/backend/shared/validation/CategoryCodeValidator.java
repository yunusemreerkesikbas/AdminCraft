package com.backend.shared.validation;

import static com.backend.shared.constants.ValidationConstants.CATEGORY_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_CATEGORY_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_CATEGORY_CODE_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_CATEGORY_CODE_SIZE;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link CategoryCode} annotation.
 * Validates that a string matches the category code pattern (allows hyphens)
 * and respects size constraints.
 */
public class CategoryCodeValidator implements ConstraintValidator<CategoryCode, String> {

  private static final Pattern CATEGORY_CODE_REGEX = Pattern.compile(CATEGORY_CODE_PATTERN);

  private CategoryCode annotation;

  @Override
  public void initialize(CategoryCode constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean required = annotation.required();
    int maxLength = annotation.maxLength();

    if (value == null || value.isEmpty()) {
      if (required) {
        setCustomMessage(context, MSG_CATEGORY_CODE_REQUIRED);
        return false;
      }
      return true;
    }

    if (value.length() > maxLength) {
      setCustomMessage(context, MSG_CATEGORY_CODE_SIZE);
      return false;
    }

    if (!CATEGORY_CODE_REGEX.matcher(value).matches()) {
      setCustomMessage(context, MSG_CATEGORY_CODE_PATTERN);
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
