package com.attendenceSystem.module.system.mapper.request;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.system.dto.request.WorkShiftTimeSettingRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;
import com.attendenceSystem.module.system.util.StringConvert;

@Component
public class WorkShiftTimeSettingRequestMapper {
    public WorkShiftTimeSettingRequest toRequest(SystemSetting setting) {
        if (setting == null) {
            return null;
        }
        return WorkShiftTimeSettingRequest.builder()
                .startWorkTime(StringConvert.toString(setting.getStartWorkTime()))
                .endWorkTime(StringConvert.toString(setting.getEndWorkTime()))
                .maxCheckinTime(StringConvert.toString(setting.getMaxCheckinTime()))
                .minCheckoutTime(StringConvert.toString(setting.getMinCheckoutTime()))
                .build();
    }

}