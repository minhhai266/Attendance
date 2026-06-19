package com.attendenceSystem.validator;

import com.attendenceSystem.annotation.PasswordMatch;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator
        implements ConstraintValidator<PasswordMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request,
            ConstraintValidatorContext context) {

        if (request.password() == null
                || request.rePassword() == null) {
            return true;
        }

        boolean valid = request.password()
                .equals(request.rePassword());

        if (!valid) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu xác nhận không khớp")
                    .addPropertyNode("rePassword")
                    .addConstraintViolation();
        }

        return valid;
    }
}