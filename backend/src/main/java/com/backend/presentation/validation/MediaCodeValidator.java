package com.backend.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.backend.shared.constants.ValidationConstants.MEDIA_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_SIZE;

public class MediaCodeValidator implements ConstraintValidator<MediaCode, String> {

    private static final Pattern MEDIA_CODE_REGEX = Pattern.compile(MEDIA_CODE_PATTERN);

    private MediaCode annotation;

    @Override
    public void initialize(MediaCode constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean required = annotation.required();
        int maxLength = annotation.maxLength();

        if (value == null || value.isEmpty()) {
            if (required) {
                setCustomMessage(context, MSG_MEDIA_CODE_REQUIRED);
                return false;
            }
            return true;
        }

        if (value.length() > maxLength) {
            setCustomMessage(context, MSG_MEDIA_CODE_SIZE);
            return false;
        }

        if (!MEDIA_CODE_REGEX.matcher(value).matches()) {
            setCustomMessage(context, MSG_MEDIA_CODE_PATTERN);
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
