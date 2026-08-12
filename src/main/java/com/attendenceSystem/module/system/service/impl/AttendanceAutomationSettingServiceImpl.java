package com.attendenceSystem.module.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.system.dto.request.AttendanceAutomationSettingsRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.mapper.request.AttendanceAutomationSettingRequestMapper;
import com.attendenceSystem.module.system.repository.SystemSettingRepository;
import com.attendenceSystem.module.system.service.AttendanceAutomationSettingService;
import com.attendenceSystem.module.system.util.StringConvert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceAutomationSettingServiceImpl implements AttendanceAutomationSettingService {

    private final SystemConfig systemConfig;
    private final SystemSettingRepository systemSettingRepository;
    private final AttendanceAutomationSettingRequestMapper attendanceAutomationSettingRequestMapper;

    @Override
    public AttendanceAutomationSettingsRequest getAttendanceAutomationSettings() {
        return attendanceAutomationSettingRequestMapper.toRequest(systemConfig.get());
    }

    @Override
    @Transactional
    public void updateAttendanceAutomationSettings(AttendanceAutomationSettingsRequest request) {
        StringConvert.fromCron(request.getAutoMarkAbsentCron());
        StringConvert.fromCron(request.getAutoHandleMissingCheckoutCron());
        StringConvert.fromCron(request.getLeaveAutoRejectCron());

        SystemSetting settings = systemSettingRepository.findById(1L)
                .orElseGet(SystemSetting::new);
        settings.setAutoMarkAbsentCron(request.getAutoMarkAbsentCron());
        settings.setAutoHandleMissingCheckoutCron(request.getAutoHandleMissingCheckoutCron());
        settings.setLeaveAutoRejectCron(request.getLeaveAutoRejectCron());
        settings.setLeaveBlackoutMinutes(request.getLeaveBlackoutMinutes());
        systemSettingRepository.save(settings);
        systemConfig.reload();
    }
}