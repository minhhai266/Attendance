package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public interface EmployeeLeaveList {
    Long getId();
    LocalDate getStartDate();
    LocalDate getEndDate();
    String getReason();
    LeaveStatus getStatus();
}
