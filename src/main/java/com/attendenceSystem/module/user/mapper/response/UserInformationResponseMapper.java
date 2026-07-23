package com.attendenceSystem.module.user.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInformationResponseMapper {

    public UserInformationResponse fromEntity(User user) {
        return UserInformationResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .specialization(user.getSpecialization())
                .department(user.getDepartment())
                .build();
    }
}