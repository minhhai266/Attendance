package com.attendenceSystem.module.attendance.dto.response;

import lombok.Builder;

@Builder
public record AttendanceHistoryStatsResponse(
        long totalDays,
        long onTime,
        long late,
        long earlyLeave,
        long totalWorkingMinutes) {
}