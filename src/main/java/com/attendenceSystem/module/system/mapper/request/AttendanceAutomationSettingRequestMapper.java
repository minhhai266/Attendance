package com.attendenceSystem.module.system.mapper.request;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.system.dto.request.AttendanceAutomationSettingsRequest;
import com.attendenceSystem.module.system.entity.SystemSetting;

@Component
public class AttendanceAutomationSettingRequestMapper {
    public AttendanceAutomationSettingsRequest toRequest(SystemSetting settings) {
        if (settings == null) {
            return null;
        }
        return AttendanceAutomationSettingsRequest.builder()
                .autoMarkAbsentCron(settings.getAutoMarkAbsentCron())
                .autoHandleMissingCheckoutCron(settings.getAutoHandleMissingCheckoutCron())
                .leaveAutoRejectCron(settings.getLeaveAutoRejectCron())
                .leaveBlackoutMinutes(settings.getLeaveBlackoutMinutes())
                .build();
    }
}
