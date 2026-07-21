package com.attendenceSystem.module.attendance.util;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Bean cung cấp TimeZone cho attendance module
 * Được cache để tối ưu hiệu suất
 */
@Slf4j
@Component
public class TimeZoneProvider {
    
    @Value("${attendance.timezone:Asia/Ho_Chi_Minh}")
    private String timezone;

    private volatile ZoneId cachedZoneId;

    public ZoneId getZoneId() {
        if (cachedZoneId == null) {
            synchronized (this) {
                if (cachedZoneId == null) {
                    try {
                        cachedZoneId = ZoneId.of(timezone);
                    } catch (Exception e) {
                        log.warn("Invalid timezone: {}, using default Asia/Ho_Chi_Minh", timezone);
                        cachedZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
                    }
                }
            }
        }
        return cachedZoneId;
    }
}