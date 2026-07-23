package com.attendenceSystem.module.attendance.util;

import java.time.LocalDateTime;

public interface AttendanceCalculator {

    boolean isLate(LocalDateTime checkInTime);

    boolean isEarlyLeave(LocalDateTime checkOutTime);

    long workingMinutes(LocalDateTime checkInTime, LocalDateTime checkOutTime);

    double totalWorkingHours(LocalDateTime checkInTime, LocalDateTime checkOutTime);
}