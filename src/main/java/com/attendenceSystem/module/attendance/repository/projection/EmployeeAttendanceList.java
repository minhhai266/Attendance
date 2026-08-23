package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

public interface EmployeeAttendanceList {
    Long getId();
    LocalDate getAttendanceDate();
    LocalDateTime getCheckInTime();
    LocalDateTime getCheckOutTime();
    AttendanceStatus getStatus();
    Set<AttendanceCheckStatus> getCheckStatuses();
    String getNote();
}