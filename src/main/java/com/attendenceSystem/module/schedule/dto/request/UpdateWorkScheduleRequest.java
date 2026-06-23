package com.attendenceSystem.module.schedule.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWorkScheduleRequest {
    private LocalDate effectiveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer allowedLateMinutes;
    private Integer allowedEarlyLeaveMinutes;
    private LocalTime missingCheckoutDeadline;
    private Set<DayOfWeek> workingDays;
}
