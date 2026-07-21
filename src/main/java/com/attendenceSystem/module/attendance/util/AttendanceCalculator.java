package com.attendenceSystem.module.attendance.util;

import java.time.Instant;

public interface AttendanceCalculator {

    boolean isLate(Instant checkInTime);

    boolean isEarlyLeave(Instant checkOutTime);

    long workingMinutes(Instant checkInTime, Instant checkOutTime);

    double totalWorkingHours(Instant checkInTime, Instant checkOutTime);
}