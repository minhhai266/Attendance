package com.attendenceSystem.module.user.dto.response;

import lombok.Builder;

    @Builder
    public record UserResponse(
            Long id,
            String username,
            String email,
            String phone,
            String fullName,
            String status,
            String role,
            String department,
            String departmentDisplay) {

    }
