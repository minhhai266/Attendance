package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record AttendanceDetailResponse(
        Long id,
        String fullName,
        LocalDate attendanceDate,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        AttendanceStatus status,
        String note,
        long workingMinutes,
        boolean late,
        boolean earlyLeave,
        String checkInImageUrl,
        String checkOutImageUrl) {
}
