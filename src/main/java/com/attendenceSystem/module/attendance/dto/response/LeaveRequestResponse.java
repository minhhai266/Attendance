package com.attendenceSystem.module.attendance.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

public record LeaveRequestResponse(
        Long id,
        String fullName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        AttendanceStatus status,
        Instant createdAt) {
}
