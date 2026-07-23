package com.attendenceSystem.module.attendance.util.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCalculatorImpl implements AttendanceCalculator {
    
    private final TimeZoneProvider timeZoneProvider;

    @Value("${attendance.start-work:08:00}")
    private String startWorkTime;

    @Value("${attendance.end-work:17:00}")
    private String endWorkTime;

    // Cache thời gian làm việc
    private volatile LocalTime cachedStartWorkTime;
    private volatile LocalTime cachedEndWorkTime;

    private ZoneId getZoneId() {
        try {
            return timeZoneProvider.getZoneId();
        } catch (Exception e) {
            log.warn("Cannot get timezone from TimeZoneProvider, using default", e);
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }

    private LocalTime getStartWorkTime() {
        if (cachedStartWorkTime == null) {
            synchronized (this) {
                if (cachedStartWorkTime == null) {
                    try {
                        cachedStartWorkTime = LocalTime.parse(startWorkTime);
                    } catch (Exception e) {
                        log.warn("Invalid start work time: {}, using default 08:00", startWorkTime);
                        cachedStartWorkTime = LocalTime.of(8, 0);
                    }
                }
            }
        }
        return cachedStartWorkTime;
    }

    private LocalTime getEndWorkTime() {
        if (cachedEndWorkTime == null) {
            synchronized (this) {
                if (cachedEndWorkTime == null) {
                    try {
                        cachedEndWorkTime = LocalTime.parse(endWorkTime);
                    } catch (Exception e) {
                        log.warn("Invalid end work time: {}, using default 17:00", endWorkTime);
                        cachedEndWorkTime = LocalTime.of(17, 0);
                    }
                }
            }
        }
        return cachedEndWorkTime;
    }

    @Override
    public boolean isLate(final LocalDateTime checkInTime) {
        if (checkInTime == null) {
            return false;
        }
        LocalTime checkIn = checkInTime.atZone(getZoneId()).toLocalTime();
        return checkIn.isAfter(getStartWorkTime());
    }

    @Override
    public boolean isEarlyLeave(final LocalDateTime checkOutTime) {
        if (checkOutTime == null) {
            return false;
        }
        LocalTime checkOut = checkOutTime.atZone(getZoneId()).toLocalTime();
        return checkOut.isBefore(getEndWorkTime());
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
}