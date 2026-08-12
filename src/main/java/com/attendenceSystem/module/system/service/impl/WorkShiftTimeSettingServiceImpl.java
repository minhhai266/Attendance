package com.attendenceSystem.module.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.system.dto.request.WorkShiftTimeSettingRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.mapper.request.WorkShiftTimeSettingRequestMapper;
import com.attendenceSystem.module.system.repository.SystemSettingRepository;
import com.attendenceSystem.module.system.service.WorkShiftTimeSettingService;
import com.attendenceSystem.module.system.util.StringConvert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkShiftTimeSettingServiceImpl implements WorkShiftTimeSettingService {

    private final SystemConfig systemConfig;
    private final SystemSettingRepository systemSettingRepository;
    private final WorkShiftTimeSettingRequestMapper workShiftTimeSettingRequestMapper;

    @Override
    public WorkShiftTimeSettingRequest getWorkShiftTimeSetting() {
        return workShiftTimeSettingRequestMapper.toRequest(systemConfig.get());
    }

    @Override
    @Transactional
    public void updateWorkShiftTimeSetting(WorkShiftTimeSettingRequest request) {
        SystemSetting settings = systemSettingRepository.findById(1L)
                .orElseGet(SystemSetting::new);

        settings.setStartWorkTime(StringConvert.fromTime(request.getStartWorkTime()));
        settings.setEndWorkTime(StringConvert.fromTime(request.getEndWorkTime()));
        settings.setMaxCheckinTime(StringConvert.fromTime(request.getMaxCheckinTime()));
        settings.setMinCheckoutTime(StringConvert.fromTime(request.getMinCheckoutTime()));

        systemSettingRepository.save(settings);
        systemConfig.reload();
    }
}