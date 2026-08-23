package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.entity.enums.Department;

import lombok.Builder;

@Builder
public record AttendanceResponse(
                Long id,
                Long userId,
                String fullName,
                Department department,
                LocalDate attendanceDate,
                LocalDateTime checkInTime,
                LocalDateTime checkOutTime,
                AttendanceStatus status,
                Set<AttendanceCheckStatus> checkStatuses,
                boolean late,
                boolean earlyLeave,
                long workingMinutes,
                String note) {
}