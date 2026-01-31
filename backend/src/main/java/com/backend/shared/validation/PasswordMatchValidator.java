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

        if (request instanceof CreateUserRequest r) {
            password = r.password();
            confirmPassword = r.confirmPassword();
        } else if (request instanceof ChangePasswordRequest r) {
            password = r.password();
            confirmPassword = r.confirmPassword();
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
