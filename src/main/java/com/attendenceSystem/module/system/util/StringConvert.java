package com.attendenceSystem.module.system.util;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringConvert {
    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public String toString(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    public LocalTime fromTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            return LocalTime.parse(time.trim(), TIME_FORMATTER);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid time format (expected HH:mm): " + time, e);
        }
    }

    public LocalTime fromCron(String cron) {
        if (!StringUtils.hasText(cron)) {
            return null;
        }

        String[] parts = cron.trim().split("\\s+");
        int hour;
        int minute;

        try {
            switch (parts.length) {
                case 6: // Cron của Spring (Giây - Phút - Giờ - Ngày - Tháng - Thứ)
                    minute = Integer.parseInt(parts[1]);
                    hour = Integer.parseInt(parts[2]);
                    break;
                case 5: // Cron chuẩn UNIX (Phút - Giờ - Ngày - Tháng - Thứ)
                    minute = Integer.parseInt(parts[0]);
                    hour = Integer.parseInt(parts[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid cron expression: " + cron);
            }

            return LocalTime.of(hour, minute);

        } catch (NumberFormatException | DateTimeException e) {
            throw new IllegalArgumentException("Invalid time format in cron expression: " + cron, e);
        }
    }

    public String toCron(LocalTime time) {
        if (time == null) {
            return null;
        }
        return String.format("0 %d %d * * *", time.getMinute(), time.getHour());
    }
}
