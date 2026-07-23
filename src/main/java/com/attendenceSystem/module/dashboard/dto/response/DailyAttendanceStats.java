package com.attendenceSystem.module.dashboard.dto.response;

public record DailyAttendanceStats(
        String dayName,
        Long present,
        Long late,
        Long absent
) {
}