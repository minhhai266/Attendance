package com.attendenceSystem.module.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.system.dto.request.UpdateSystemSettingRequest;
import com.attendenceSystem.module.system.dto.response.SystemSettingResponse;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.mapper.response.SystemSettingResponseMapper;
import com.attendenceSystem.module.system.repository.SystemSettingRepository;
import com.attendenceSystem.module.system.service.SystemSettingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {
    private static final Long SETTING_ID = 1L;
    private final SystemSettingRepository systemSettingRepository;
    private final SystemSettingResponseMapper systemSettingResponseMapper;

    @Override
    public SystemSettingResponse getSetting() {
        SystemSetting setting = systemSettingRepository.findById(SETTING_ID)
                .orElseGet(this::createDefaultSetting);

        return systemSettingResponseMapper.fromEntity(setting);
    }

    @Override
    @Transactional
    public SystemSettingResponse updateSetting(
            UpdateSystemSettingRequest request) {

        SystemSetting setting = systemSettingRepository.findById(SETTING_ID)
                .orElseGet(this::createDefaultSetting);

        if (request.getRecognitionThreshold() != null) {
            setting.setRecognitionThreshold(request.getRecognitionThreshold());
        }
        if (request.getCooldownSeconds() != null) {
            setting.setCooldownSeconds(request.getCooldownSeconds());
        }
        if (request.getFrameSkip() != null) {
            setting.setFrameSkip(request.getFrameSkip());
        }
        if (request.getAntiSpoofingEnabled() != null) {
            setting.setAntiSpoofingEnabled(request.getAntiSpoofingEnabled());
        }
        if (request.getCheckInCameraSource() != null) {
            setting.setCheckInCameraSource(request.getCheckInCameraSource());
        }
        if (request.getCheckOutCameraSource() != null) {
            setting.setCheckOutCameraSource(request.getCheckOutCameraSource());
        }

        SystemSetting updated = systemSettingRepository.save(setting);
        return systemSettingResponseMapper.fromEntity(updated);
    }

    @Transactional
    private SystemSetting createDefaultSetting() {
        SystemSetting setting = SystemSetting.builder()
                .id(SETTING_ID)
                .recognitionThreshold(0.55)
                .cooldownSeconds(30)
                .frameSkip(1)
                .antiSpoofingEnabled(true)
                .build();

        return systemSettingRepository.save(setting);
    }
}
