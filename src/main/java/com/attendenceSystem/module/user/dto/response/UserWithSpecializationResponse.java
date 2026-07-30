package com.attendenceSystem.module.user.dto.response;

import com.attendenceSystem.module.user.entity.enums.Specialization;

public record UserWithSpecializationResponse(Long id, String fullName, Specialization specialization) {
}