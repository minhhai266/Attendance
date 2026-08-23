package com.attendenceSystem.module.attendance.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.repository.projection.UserNameProjection;

public interface ManagerAttendanceList {
    Long getId();
    UserNameProjection getUser();
    LocalDate getAttendanceDate();
    LocalDateTime getCheckInTime();
    LocalDateTime getCheckOutTime();
    AttendanceStatus getStatus();
    Set<AttendanceCheckStatus>  getCheckStatuses(); 
    String getNote();
}