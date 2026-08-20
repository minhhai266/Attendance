package com.attendenceSystem.module.dashboard.dto.response;

import org.springframework.data.domain.Page;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;

public record EmployeeDashboardResponse(

        Long totalReports,
        String attendanceRate,
        Page<EmployeeAttendanceListResponse> attendanceHistory

) {
}