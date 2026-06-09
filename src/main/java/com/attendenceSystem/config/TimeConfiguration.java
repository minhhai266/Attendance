package com.attendenceSystem.config;

import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.attendenceSystem.config.properties.AppProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TimeConfiguration {

    private final AppProperties appProperties;

    @Bean
    public ZoneId applicationZoneId() {
        return ZoneId.of(appProperties.getTimezone());
    }
}