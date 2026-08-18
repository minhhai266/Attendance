package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public interface LeaveDetail {
    Long getId();
    String getUserFullName();
    LocalDate getStartDate();
    LocalDate getEndDate();
    LeaveStatus getStatus();
    String getReason();
}
