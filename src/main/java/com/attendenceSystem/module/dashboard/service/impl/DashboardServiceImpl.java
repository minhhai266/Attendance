package com.attendenceSystem.module.dashboard.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.provider.AttendanceStatisticsProvider;
import com.attendenceSystem.module.dashboard.dto.response.AccountTypeDistributionResponse;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.DailyAttendanceStats;
import com.attendenceSystem.module.dashboard.dto.response.EmployeeDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.mapper.response.DashboardResponseMapper;
import com.attendenceSystem.module.dashboard.service.DashboardService;
import com.attendenceSystem.module.dashboard.util.DashboardCalculator;
import com.attendenceSystem.module.report.provider.ReportStatisticsProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.provider.UserContextProvider;
import com.attendenceSystem.module.user.provider.UserStatisticProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
        private final UserContextProvider userContextProvider;
        private final AttendanceStatisticsProvider attendanceStatisticsProvider;
        private final UserStatisticProvider userStatisticProvider;
        private final ReportStatisticsProvider reportStatisticsProvider;
        private final DashboardResponseMapper dashboardResponseMapper;

        @Override
        public AdminDashboardResponse getAdminDashboard() {
                long totalAccounts = userStatisticProvider.getTotalAccounts();
                long activeAccounts = userStatisticProvider.getAccountsByStatus(Status.ACTIVE);
                long inactiveAccounts = userStatisticProvider.getAccountsByStatus(Status.INACTIVE);
                long pendingAccounts = userStatisticProvider.getAccountsByStatus(Status.PENDING);
                var accountTypeDistribution = userStatisticProvider.getRoleCounts()
                                .entrySet()
                                .stream()
                                .map(entry -> new AccountTypeDistributionResponse(
                                                entry.getKey(),
                                                roleLabel(entry.getKey()),
                                                entry.getValue()))
                                .toList();
                return dashboardResponseMapper.toAdminDashboardResponse(
                                totalAccounts,
                                activeAccounts,
                                inactiveAccounts,
                                pendingAccounts,
                                accountTypeDistribution);
        }

        @Override
        public ManagerDashboardResponse getManagerDashboard() {
                LocalDate today = LocalDate.now();

                long totalEmployees = userStatisticProvider.getTotalEmployeeAndManager();
                long presentToday = attendanceStatisticsProvider
                                .getCountByDateAndStatus(today, AttendanceStatus.PRESENT);
                long lateToday = attendanceStatisticsProvider
                                .getCountByDateAndStatus(today, AttendanceStatus.LATE);
                long attendedToday = presentToday + lateToday;
                long absentToday = Math.max(0, totalEmployees - attendedToday);
                Page<AttendanceResponse> attendanceHistory = attendanceStatisticsProvider
                                .getRecentHistory(10);
                var monday = TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY).adjustInto(today);
                var weeklyStats = IntStream.rangeClosed(2, 6)
                                .mapToObj(dayOfWeek -> {
                                        LocalDate date = ((LocalDate) monday)
                                                        .plusDays(dayOfWeek - 2);
                                        long present = attendanceStatisticsProvider
                                                        .getCountByDateAndStatus(date,
                                                                        AttendanceStatus.PRESENT);
                                        long late = attendanceStatisticsProvider
                                                        .getCountByDateAndStatus(date,
                                                                        AttendanceStatus.LATE);
                                        long attended = present + late;
                                        long absent = Math.max(0, totalEmployees - attended);
                                        return new DailyAttendanceStats(
                                                        "T" + dayOfWeek,
                                                        present,
                                                        late,
                                                        absent);
                                })
                                .toList();

                return dashboardResponseMapper.toManagerDashboardResponse(
                                totalEmployees,
                                presentToday,
                                lateToday,
                                absentToday,
                                attendanceHistory,
                                weeklyStats);
        }

        @Override
        public EmployeeDashboardResponse getEmployeeDashboard() {
                User user = userContextProvider.getCurrentUserEntity();
                long totalReports = reportStatisticsProvider.countReportsByEmployee(user);
                long totalDays = attendanceStatisticsProvider.getTotalDaysByUser(user);
                long attendedDays = attendanceStatisticsProvider.getAttendedDaysByUser(user);
                String attendanceRate = DashboardCalculator.showResultStr(attendedDays, totalDays);
                Page<AttendanceResponse> attendanceHistory = attendanceStatisticsProvider
                                .getRecentHistoryByUser(user, 10);
                return dashboardResponseMapper.toEmployeeDashboardResponse(
                                totalReports,
                                attendanceRate,
                                attendanceHistory);
        }

        private String roleLabel(String roleName) {
                if (!StringUtils.hasText(roleName)) {
                        return "";
                }
                String lower = roleName.toLowerCase(Locale.ROOT);
                return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
        }
}
