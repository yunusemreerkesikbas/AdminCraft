package com.backend.shared.validation;

import java.lang.reflect.Field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        try {
            String password = getFieldValue(request, passwordField);
            String confirmPassword = getFieldValue(request, confirmPasswordField);

            if (password == null || confirmPassword == null) {
                return true;
            }

            boolean isValid = password.equals(confirmPassword);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(confirmPasswordField)
                        .addConstraintViolation();
            }

            return isValid;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    String.format("Fields '%s' or '%s' not found in class %s. Please verify field names in @PasswordMatch annotation.",
                            passwordField, confirmPasswordField, request.getClass().getName()),
                    e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    String.format("Cannot access fields '%s' or '%s' in class %s",
                            passwordField, confirmPasswordField, request.getClass().getName()),
                    e);
        }
    }

    private String getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(object);
        return value != null ? value.toString() : null;
    }
}
