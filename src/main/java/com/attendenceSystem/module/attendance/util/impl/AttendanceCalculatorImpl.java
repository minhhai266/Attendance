package com.attendenceSystem.module.attendance.util.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCalculatorImpl implements AttendanceCalculator {

    @Value("${attendance.start-work:08:00}")
    private String startWorkTimeStr;

    @Value("${attendance.end-work:17:00}")
    private String endWorkTimeStr;

    // Cache thời gian làm việc
    private LocalTime startWorkTime;
    private LocalTime endWorkTime;

    @PostConstruct
    public void init() {
        // Init Start Time
        try {
            this.startWorkTime = LocalTime.parse(startWorkTimeStr);
        } catch (Exception e) {
            log.warn("Invalid start work time: {}, using default 08:00", startWorkTimeStr);
            this.startWorkTime = LocalTime.of(8, 0);
        }

        // Init End Time
        try {
            this.endWorkTime = LocalTime.parse(endWorkTimeStr);
        } catch (Exception e) {
            log.warn("Invalid end work time: {}, using default 17:00", endWorkTimeStr);
            this.endWorkTime = LocalTime.of(17, 0);
        }
    }

    @Override
    public boolean isLate(final LocalDateTime checkInTime) {
        if (checkInTime == null) {
            return false;
        }
        return checkInTime.toLocalTime().isAfter(startWorkTime);
    }

    @Override
    public boolean isEarlyLeave(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        return checkOutTime.toLocalTime().isBefore(endWorkTime);
    }

    @Override
    public long workingMinutes(final LocalDateTime checkInTime, final LocalDateTime checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            return 0;
        }
        return Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    @Override
    public double totalWorkingHours(final LocalDateTime checkInTime, final LocalDateTime checkOutTime) {
        long minutes = workingMinutes(checkInTime, checkOutTime);
        return minutes / 60.0;
    }

    @Override
    public boolean isPastAllowedCheckInTime(final LocalDateTime checkInTime) {
        if(checkInTime == null) {
            return false;
        }
        return checkInTime.toLocalTime().isAfter(endWorkTime);
    }
}