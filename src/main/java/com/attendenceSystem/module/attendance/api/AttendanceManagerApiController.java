package com.attendenceSystem.module.attendance.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.Attendance.ROOT + "/manager")
@RequiredArgsConstructor
public class AttendanceManagerApiController {

    private final AttendanceService attendanceService;

    @GetMapping("/stats")
    public ResponseEntity<ManagerStatsResponse> getStats(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status) {
        com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus attendanceStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                attendanceStatus = AttendanceStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        ManagerStatsResponse stats = attendanceService.getManagerStats(departmentId, startDate, endDate, attendanceStatus);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AttendanceResponse>> getList(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status) {
        AttendanceStatus attendanceStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                attendanceStatus = AttendanceStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        List<AttendanceResponse> list = attendanceService.getManagerAttendanceList(startDate, endDate, attendanceStatus);
        return ResponseEntity.ok(list);
    }
}