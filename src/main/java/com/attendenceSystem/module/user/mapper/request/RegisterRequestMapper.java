package com.attendenceSystem.module.user.mapper.request;

import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.entity.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisterRequestMapper {
    public static User toEntity(RegisterRequest request, String hashPassword) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(hashPassword)
                .fullName(request.fullName())
                .build();
    }
}
