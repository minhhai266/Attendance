package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public interface ManagerLeaveList {
    Long getId();
    String getUserFullName();
    LocalDate getStartDate();
    LocalDate getEndDate();
    String getReason();
    LeaveStatus getStatus();
}
