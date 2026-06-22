package com.attendenceSystem.module.attendance.dto.response;

import java.time.Instant;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record AttendanceResponse(
        Long id,
        Long userId,
        String fullName,
        Instant checkInTime,
        Instant checkOutTime,
        AttendanceStatus status,
        String note) {
}
