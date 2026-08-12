package com.attendenceSystem.module.system.service;

import com.attendenceSystem.module.system.dto.request.WorkShiftTimeSettingRequest;

public interface WorkShiftTimeSettingService {
    WorkShiftTimeSettingRequest getWorkShiftTimeSetting();

    void updateWorkShiftTimeSetting(WorkShiftTimeSettingRequest request);

}
