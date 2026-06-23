package com.attendenceSystem.module.schedule.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record WorkScheduleResponse(
        Long id,
        LocalDate effectiveDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer allowedLateMinutes,
        Integer allowedEarlyLeaveMinutes,
        LocalTime missingCheckoutDeadline,
        Set<DayOfWeek> workingDays) {
}
