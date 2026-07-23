package com.attendenceSystem.module.dashboard.dto.response;

import org.springframework.data.domain.Page;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;

import java.util.List;

public record ManagerDashboardResponse(
        Long totalEmployees,

        Long attendedEmployees,

        Long lateEmployees,

        Long absentEmployees,

        Page<AttendanceResponse> attendanceHistory,

        List<DailyAttendanceStats> weeklyStats

) {
}
