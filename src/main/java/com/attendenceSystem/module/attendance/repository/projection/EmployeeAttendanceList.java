package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;

public interface EmployeeAttendanceList {
    Long getId();
    LocalDate getAttendanceDate();
    LocalDateTime getCheckInTime();
    LocalDateTime getCheckOutTime();
    AttendanceStatus getStatus();
    String getNote();
}
