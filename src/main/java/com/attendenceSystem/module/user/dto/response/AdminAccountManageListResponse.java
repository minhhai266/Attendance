package com.attendenceSystem.module.user.dto.response;


import lombok.Builder;

@Builder
public record AdminAccountManageListResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String status) {
}
