package com.attendenceSystem.module.user.dto.response;

import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Specialization;
import com.attendenceSystem.module.user.entity.enums.Status;

import lombok.Builder;

@Builder
public record UserInformationResponse(
        Long id,
        String username,
        String email,
        String phone,
        String fullName,
        Role role,
        Status status,
        Specialization specialization,
        Department department) {

}