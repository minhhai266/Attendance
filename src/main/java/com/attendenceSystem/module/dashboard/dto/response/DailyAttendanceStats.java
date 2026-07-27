package com.attendenceSystem.module.dashboard.dto.response;

public record DailyAttendanceStats(
        String dayName,
        long present,
        long late,
        long absent
) {
}