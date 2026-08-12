package com.attendenceSystem.module.attendance.util.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.system.entity.SystemSetting;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceCalculatorImpl implements AttendanceCalculator {

    private static final LocalTime DEFAULT_START_WORK_TIME = LocalTime.of(8, 30);
    private static final LocalTime DEFAULT_MAX_CHECK_IN_TIME = LocalTime.of(10, 0);
    private static final LocalTime DEFAULT_MIN_CHECK_OUT_TIME = LocalTime.of(16, 0);
    private static final LocalTime DEFAULT_END_WORK_TIME = LocalTime.of(17, 30);

    private final SystemConfig systemConfig;

    private LocalTime startWorkTime() {
        SystemSetting s = systemConfig.get();
        return s.getStartWorkTime() != null ? s.getStartWorkTime() : DEFAULT_START_WORK_TIME;
    }

    private LocalTime maxCheckInTime() {
        SystemSetting s = systemConfig.get();
        return s.getMaxCheckinTime() != null ? s.getMaxCheckinTime() : DEFAULT_MAX_CHECK_IN_TIME;
    }

    private LocalTime minCheckOutTime() {
        SystemSetting s = systemConfig.get();
        return s.getMinCheckoutTime() != null ? s.getMinCheckoutTime() : DEFAULT_MIN_CHECK_OUT_TIME;
    }

    private LocalTime endWorkTime() {
        SystemSetting s = systemConfig.get();
        return s.getEndWorkTime() != null ? s.getEndWorkTime() : DEFAULT_END_WORK_TIME;
    }

    @Override
    public boolean isLate(final LocalDateTime checkInTime) {
        if (checkInTime == null) {
            return false;
        }
        LocalTime time = checkInTime.toLocalTime();
        return time.isAfter(startWorkTime()) && time.isBefore(maxCheckInTime());
    }

    @Override
    public boolean isEarlyLeave(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        LocalTime time = checkOutTime.toLocalTime();
        return time.isBefore(endWorkTime()) && time.isAfter(minCheckOutTime());
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
        return checkInTime.toLocalTime().isAfter(maxCheckInTime());
    }

    @Override
    public boolean isBeforeMinCheckOutTime(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        return checkOutTime.toLocalTime().isBefore(minCheckOutTime());
    }
}