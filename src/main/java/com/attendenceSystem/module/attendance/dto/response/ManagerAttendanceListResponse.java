package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record ManagerAttendanceListResponse(
        String userFullName,
        LocalDate attendanceDate,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        AttendanceStatus status,
        String note,
        boolean late,
        boolean earlyLeave,
        long workingMinutes) {

}
