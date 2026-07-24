package com.attendenceSystem.module.user.mapper.response;


import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    public UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .status(user.getStatus() == null ? null : user.getStatus().name())
                .role(user.getRole().name())
                .department(user.getDepartment() == null ? null : user.getDepartment().getRoomCode())
                .departmentDisplay(user.getDepartment() == null ? "N/A" : user.getDepartment().getDisplayName())
                .build();
    }
}
