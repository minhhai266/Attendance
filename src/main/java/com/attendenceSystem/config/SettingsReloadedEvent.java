package com.attendenceSystem.config;

import com.attendenceSystem.module.system.entity.SystemSetting;

import lombok.Getter;

/**
 * Event được phát ra mỗi khi {@link SystemConfig} reload cấu hình hệ thống.
 * Các bean lắng nghe (VD: SystemScheduler) sẽ refresh lại job của mình.
 */
@Getter
public class SettingsReloadedEvent {

    private final Object source;
    private final SystemSetting settings;

    public SettingsReloadedEvent(Object source, SystemSetting settings) {
        this.source = source;
        this.settings = settings;
    }
}