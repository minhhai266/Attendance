package com.attendenceSystem.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.attendenceSystem.config.properties.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TimeConfiguration {

    private static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    private final AppProperties appProperties;

    @Bean
    public ZoneId applicationZoneId() {
        try {
            return ZoneId.of(appProperties.getTimezone());
        } catch (Exception e) {
            log.warn("Invalid timezone: {}, using default {}", appProperties.getTimezone(), DEFAULT_TIMEZONE);
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    @Bean
    public Clock applicationClock(ZoneId applicationZoneId) {
        return Clock.system(applicationZoneId);
    }
}