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

    @Value("${attendance.start-work:08:30}")
    private String startWorkTimeStr;

    @Value("${attendance.max-check-in-time:10:00}")
    private String maxCheckInTimeStr;

    @Value("${attendance.min-check-out-time:16:00}")
    private String minCheckOutTimeStr;

    @Value("${attendance.end-work:17:30}")
    private String endWorkTimeStr;

    private LocalTime startWorkTime;
    private LocalTime maxCheckInTime;
    private LocalTime minCheckOutTime;
    private LocalTime endWorkTime;

    @PostConstruct
    public void init() {
        try {
            this.startWorkTime = LocalTime.parse(startWorkTimeStr);
        } catch (Exception e) {
            log.warn("Invalid start work time: {}, using default 08:30", startWorkTimeStr);
            this.startWorkTime = LocalTime.of(8, 30);
        }

        try {
            this.maxCheckInTime = LocalTime.parse(maxCheckInTimeStr);
        } catch (Exception e) {
            log.warn("Invalid max check-in time: {}, using default 10:00", maxCheckInTimeStr);
            this.maxCheckInTime = LocalTime.of(10, 0);
        }

        try {
            this.minCheckOutTime = LocalTime.parse(minCheckOutTimeStr);
        } catch (Exception e) {
            log.warn("Invalid min check-out time: {}, using default 16:00", minCheckOutTimeStr);
            this.minCheckOutTime = LocalTime.of(16, 0);
        }

        try {
            this.endWorkTime = LocalTime.parse(endWorkTimeStr);
        } catch (Exception e) {
            log.warn("Invalid end work time: {}, using default 17:30", endWorkTimeStr);
            this.endWorkTime = LocalTime.of(17, 30);
        }
    }

    @Override
    public boolean isLate(final LocalDateTime checkInTime) {
        if (checkInTime == null) {
            return false;
        }
        LocalTime time = checkInTime.toLocalTime();
        return time.isAfter(startWorkTime) && time.isBefore(maxCheckInTime);
    }

    @Override
    public boolean isEarlyLeave(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        LocalTime time = checkOutTime.toLocalTime();
        return time.isBefore(endWorkTime) && time.isAfter(minCheckOutTime);
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
        if (checkInTime == null) {
            return false;
        }
        return checkInTime.toLocalTime().isAfter(maxCheckInTime);
    }

    @Override
    public boolean isBeforeMinCheckOutTime(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        return checkOutTime.toLocalTime().isBefore(minCheckOutTime);
    }
}