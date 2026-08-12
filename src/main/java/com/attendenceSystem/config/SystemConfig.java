package com.attendenceSystem.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.repository.SystemSettingRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfig {

    private final SystemSettingRepository systemSettingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private volatile SystemSetting settings;

    @PostConstruct
    public void init() {
        reload();
    }

    public synchronized void reload() {
        this.settings = systemSettingRepository.findById(1L)
                .orElseGet(() -> {
                    log.warn("Chưa có cấu hình hệ thống (id=1). Tạo mới cấu hình mặc định.");
                    SystemSetting defaultSetting = SystemSetting.builder()
                            .id(1L)
                            .build();
                    return systemSettingRepository.save(defaultSetting);
                });
        // Phát event để các bean khác (SystemScheduler, ...) re-schedule/refresh
        eventPublisher.publishEvent(new SettingsReloadedEvent(this, this.settings));
    }

    public SystemSetting get() {
        if (settings == null) {
            throw new IllegalStateException("Chưa có config hệ thống. Vui lòng thiết lập config trước khi sử dụng hệ thống.");
        }
        return settings;
    }
}