package com.attendenceSystem.module.dashboard.dto.response;

import org.springframework.data.domain.Page;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;

public record ManagerDashboardResponse(

        Long totalEmployees,

        Long attendedEmployees,

        Long absentEmployees,

        Page<AttendanceResponse> attendanceHistory

) {
}