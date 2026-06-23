package com.attendenceSystem.module.attendance.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record AttendanceResponse(
                Long id,
                Long userId,
                String fullName,
                LocalDate attendanceDate,
                Instant checkInTime,
                Instant checkOutTime,
                AttendanceStatus status,
                boolean late,
                boolean earlyLeave,
                long workingMinutes,
                String note) {
}
