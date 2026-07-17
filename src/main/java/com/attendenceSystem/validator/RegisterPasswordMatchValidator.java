package com.attendenceSystem.validator;

import com.attendenceSystem.annotation.RegisterPasswordMatch;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegisterPasswordMatchValidator
        implements ConstraintValidator<RegisterPasswordMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request,
            ConstraintValidatorContext context) {

        if (request.getPassword() == null
                || request.getRePassword() == null) {
            return true;
        }

        boolean valid = request.getPassword()
                .equals(request.getRePassword());

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
