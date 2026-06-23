package com.attendenceSystem.module.system.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.system.dto.response.SystemSettingResponse;
import com.attendenceSystem.module.system.entity.SystemSetting;

@Component
public class SystemSettingResponseMapper {

    public SystemSettingResponse fromEntity(SystemSetting systemSetting) {
        if (systemSetting == null) {
            return null;
        }
        return SystemSettingResponse.builder()
                .id(systemSetting.getId())
                .recognitionThreshold(systemSetting.getRecognitionThreshold())
                .cooldownSeconds(systemSetting.getCooldownSeconds())
                .frameSkip(systemSetting.getFrameSkip())
                .antiSpoofingEnabled(systemSetting.getAntiSpoofingEnabled())
                .checkInCameraSource(systemSetting.getCheckInCameraSource())
                .checkOutCameraSource(systemSetting.getCheckOutCameraSource())
                .build();
    }
}
