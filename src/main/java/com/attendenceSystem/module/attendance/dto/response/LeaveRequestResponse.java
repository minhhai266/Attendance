package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public record LeaveRequestResponse(
        Long id,
        String fullName,
        String departmentDisplay,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        LocalDateTime createdAt) {
}
