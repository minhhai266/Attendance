package com.attendenceSystem.module.system.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.system.dto.request.AttendanceAutomationSettingsRequest;
import com.attendenceSystem.module.system.dto.request.EmailConfigRequest;
import com.attendenceSystem.module.system.dto.request.WorkShiftTimeSettingRequest;
import com.attendenceSystem.module.system.service.AttendanceAutomationSettingService;
import com.attendenceSystem.module.system.service.EmailSettingService;
import com.attendenceSystem.module.system.service.WorkShiftTimeSettingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.System.ROOT)
@RequiredArgsConstructor
public class SystemApiController {

    private final EmailSettingService emailSettingService;
    private final WorkShiftTimeSettingService workShiftTimeSettingService;
    private final AttendanceAutomationSettingService attendanceAutomationSettingService;

    // ===== EMAIL SETTINGS =====
    @GetMapping(Routes.System.EMAIL)
    public ResponseEntity<EmailConfigRequest> getEmailConfig() {
        return ResponseEntity.ok(emailSettingService.getEmailConfigMasked());
    }

    @PutMapping(Routes.System.EMAIL)
    public ResponseEntity<Void> updateEmailConfig(@Valid @RequestBody EmailConfigRequest request) {
        emailSettingService.updateEmailConfig(request);
        return ResponseEntity.noContent().build();
    }

    // ===== WORK SHIFT TIME SETTINGS =====
    @GetMapping(Routes.System.WORK_SHIFT_TIME)
    public ResponseEntity<WorkShiftTimeSettingRequest> getWorkShiftTime() {
        return ResponseEntity.ok(workShiftTimeSettingService.getWorkShiftTimeSetting());
    }

    @PutMapping(Routes.System.WORK_SHIFT_TIME)
    public ResponseEntity<Void> updateWorkShiftTime(@Valid @RequestBody WorkShiftTimeSettingRequest request) {
        workShiftTimeSettingService.updateWorkShiftTimeSetting(request);
        return ResponseEntity.noContent().build();
    }

    // ===== AUTOMATION SETTINGS =====
    @GetMapping(Routes.System.AUTOMATION)
    public ResponseEntity<AttendanceAutomationSettingsRequest> getAutomationSettings() {
        return ResponseEntity.ok(attendanceAutomationSettingService.getAttendanceAutomationSettings());
    }

    @PutMapping(Routes.System.AUTOMATION)
    public ResponseEntity<Void> updateAutomationSettings(@Valid @RequestBody AttendanceAutomationSettingsRequest request) {
        attendanceAutomationSettingService.updateAttendanceAutomationSettings(request);
        return ResponseEntity.noContent().build();
    }
}
