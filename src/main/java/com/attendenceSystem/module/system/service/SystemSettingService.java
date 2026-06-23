package com.attendenceSystem.module.system.service;

import com.attendenceSystem.module.system.dto.request.UpdateSystemSettingRequest;
import com.attendenceSystem.module.system.dto.response.SystemSettingResponse;

public interface SystemSettingService {
    SystemSettingResponse getSetting();

    SystemSettingResponse updateSetting(UpdateSystemSettingRequest request);
}
