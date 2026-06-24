package com.attendenceSystem.module.user.mapper.request;

import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisterRequestMapper {
    public static User toEntity(RegisterRequest request, String hashPassword) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashPassword)
                .fullName(request.getFullName())
                .role(Role.STUDENT)
                .mustChangePassword(false)
                .build();
    }
}
