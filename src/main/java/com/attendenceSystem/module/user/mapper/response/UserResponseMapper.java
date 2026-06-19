package com.attendenceSystem.module.user.mapper.response;

import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private final ZoneId applicationZoneId;

    public UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.isActive())
                .role(user.getRole().name())
                .build();
    }
}