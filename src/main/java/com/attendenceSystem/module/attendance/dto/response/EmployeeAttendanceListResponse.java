package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record EmployeeAttendanceListResponse(
                Long id,
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