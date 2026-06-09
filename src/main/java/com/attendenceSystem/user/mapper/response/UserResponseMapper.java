package com.attendenceSystem.user.mapper.response;

import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.attendenceSystem.user.dto.response.UserResponse;
import com.attendenceSystem.user.entity.User;
import com.attendenceSystem.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private final ZoneId applicationZoneId;

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(
                        TimeUtil.formatInstant(
                                user.getCreatedAt(),
                                applicationZoneId))
                .updatedAt(
                        TimeUtil.formatInstant(
                                user.getUpdatedAt(),
                                applicationZoneId))
                .deleted(user.getDeleted())
                .build();
    }
}