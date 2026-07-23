package com.attendenceSystem.module.dashboard.mapper.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.dashboard.dto.response.AccountTypeDistributionResponse;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.EmployeeDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.DailyAttendanceStats;

@Component
public class DashboardResponseMapper {

    public AdminDashboardResponse toAdminDashboardResponse(long totalAccounts, long activeAccounts,
            long inactiveAccounts, long pendingAccounts,
            List<AccountTypeDistributionResponse> accountTypeDistribution) {
        return new AdminDashboardResponse(totalAccounts, activeAccounts, inactiveAccounts, pendingAccounts,
                accountTypeDistribution);
    }

    public ManagerDashboardResponse toManagerDashboardResponse(long totalEmployees, long attendedEmployees,
            long lateEmployees, long absentEmployees, Page<AttendanceResponse> attendanceHistory,
            List<DailyAttendanceStats> weeklyStats) {
        return new ManagerDashboardResponse(totalEmployees, attendedEmployees, lateEmployees, absentEmployees, attendanceHistory, weeklyStats);
    }

    public EmployeeDashboardResponse toEmployeeDashboardResponse(long totalReports, long acceptedReports,
            long rejectedReports, String attendanceRate, Page<AttendanceResponse> attendanceHistory) {
        return new EmployeeDashboardResponse(totalReports, acceptedReports, rejectedReports, attendanceRate,
                attendanceHistory);
    }
}
