package com.attendenceSystem.module.attendance.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AttendanceCalculator {
        @Value("${attendance.timezone:Asia/Ho_Chi_Minh}")
        private String timezone;

        @Value("${attendance.start-work:08:00}")
        private String startWorkTime;

        @Value("${attendance.end-work:17:00}")
        private String endWorkTime;

        private ZoneId getZoneId() {
                try {
                        return ZoneId.of(timezone);
                } catch (Exception e) {
                        log.warn("Invalid timezone: {}, using default Asia/Ho_Chi_Minh", timezone);
                        return ZoneId.of("Asia/Ho_Chi_Minh");
                }
        }

        private LocalTime getStartWorkTime() {
                try {
                        return LocalTime.parse(startWorkTime);
                } catch (Exception e) {
                        log.warn("Invalid start work time: {}, using default 08:00", startWorkTime);
                        return LocalTime.of(8, 0);
                }
        }

        private LocalTime getEndWorkTime() {
                try {
                        return LocalTime.parse(endWorkTime);
                } catch (Exception e) {
                        log.warn("Invalid end work time: {}, using default 17:00", endWorkTime);
                        return LocalTime.of(17, 0);
                }
        }

        public boolean isLate(Instant checkInTime) {
                if (checkInTime == null) {
                        return false;
                }
                LocalTime checkIn = checkInTime.atZone(getZoneId()).toLocalTime();
                return checkIn.isAfter(getStartWorkTime());
        }

        public boolean isEarlyLeave(Instant checkOutTime) {
                if (checkOutTime == null) {
                        return false;
                }
                LocalTime checkOut = checkOutTime.atZone(getZoneId()).toLocalTime();
                return checkOut.isBefore(getEndWorkTime());
        }

        public long workingMinutes(Instant checkInTime, Instant checkOutTime) {
                if (checkInTime == null || checkOutTime == null) {
                        return 0;
                }
                return Duration.between(checkInTime, checkOutTime).toMinutes();
        }
}