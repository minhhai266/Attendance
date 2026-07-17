package com.attendenceSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.attendenceSystem.validator.UpdatePasswordMatchValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UpdatePasswordMatchValidator.class)
public @interface UpdatePasswordMatch {
    String message() default "Mật khẩu xác nhận không khớp";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
