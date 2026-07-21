package com.attendenceSystem.module.attendance.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public record LeaveDetailResponse(
        Long id,
        String username,
        String fullName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        Instant createdAt) {
}
