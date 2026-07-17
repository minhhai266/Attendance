package com.attendenceSystem.validator;

import com.attendenceSystem.annotation.UpdatePasswordMatch;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdatePasswordMatchValidator implements
        ConstraintValidator<UpdatePasswordMatch, UpdatePasswordRequest> {

    @Override
    public boolean isValid(UpdatePasswordRequest request, ConstraintValidatorContext context) {
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return false;
        }
        return request.getPassword().equals(request.getConfirmPassword());
    }

}
