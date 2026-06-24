package com.attendenceSystem.module.dashboard.mapper.response;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.StudentDashboardResponse;

@Component
public class DashboardResponseMapper {

    public AdminDashboardResponse toAdminDashboardResponse(long totalAccounts, long activeAccounts,
            long inactiveAccounts, long pendingAccounts) {
        return new AdminDashboardResponse(totalAccounts, activeAccounts, inactiveAccounts, pendingAccounts);
    }

    public ManagerDashboardResponse toManagerDashboardResponse(long totalEmployees, long attendedEmployees,
            long absentEmployees, Page<AttendanceResponse> attendanceHistory) {
        return new ManagerDashboardResponse(totalEmployees, attendedEmployees, absentEmployees, attendanceHistory);
    }

    public StudentDashboardResponse toStudentDashboardResponse(long totalReports, long acceptedReports,
            long rejectedReports, String attendanceRate, Page<AttendanceResponse> attendanceHistory) {
        return new StudentDashboardResponse(totalReports, acceptedReports, rejectedReports, attendanceRate,
                attendanceHistory);
    }
}
