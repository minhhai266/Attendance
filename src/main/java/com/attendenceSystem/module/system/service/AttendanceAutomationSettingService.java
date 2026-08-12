package com.attendenceSystem.module.system.service;


import com.attendenceSystem.module.system.dto.request.AttendanceAutomationSettingsRequest;


public interface AttendanceAutomationSettingService {
    
    AttendanceAutomationSettingsRequest getAttendanceAutomationSettings();

    void updateAttendanceAutomationSettings(AttendanceAutomationSettingsRequest request);
    
}
