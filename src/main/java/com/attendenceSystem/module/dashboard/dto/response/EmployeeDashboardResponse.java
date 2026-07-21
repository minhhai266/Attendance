package com.attendenceSystem.module.dashboard.dto.response;

import org.springframework.data.domain.Page;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;

public record EmployeeDashboardResponse(

        Long totalReports,
        Long acceptedReports,
        Long rejectedReports,
        String attendanceRate,
        Page<AttendanceResponse> attendanceHistory

) {
}