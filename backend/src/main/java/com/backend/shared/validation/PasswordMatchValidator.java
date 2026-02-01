package com.backend.shared.validation;

import com.backend.presentation.dto.request.ChangePasswordRequest;
import com.backend.presentation.dto.request.CreateUserRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        String password = null;
        String confirmPassword = null;
        boolean supportedType = false;

        if (request instanceof CreateUserRequest r) {
            password = r.password();
            confirmPassword = r.confirmPassword();
            supportedType = true;
        } else if (request instanceof ChangePasswordRequest r) {
            password = r.password();
            confirmPassword = r.confirmPassword();
            supportedType = true;
        }

        if (!supportedType) {
            throw new IllegalArgumentException("@PasswordMatch is not supported for type: " + request.getClass().getName());
        }

        if (password == null || confirmPassword == null) {
            return true;
        }

        boolean isValid = password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
