package com.attendenceSystem.module.dashboard.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.dashboard.dto.response.AccountTypeDistributionResponse;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.DailyAttendanceStats;
import com.attendenceSystem.module.dashboard.dto.response.EmployeeDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.mapper.response.DashboardResponseMapper;
import com.attendenceSystem.module.dashboard.service.DashboardService;
import com.attendenceSystem.module.dashboard.util.DashboardCalculator;
import com.attendenceSystem.module.report.entity.enums.ReportStatus;
import com.attendenceSystem.module.report.repository.ReportRepository;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
        private final UserRepository userRepository;
        private final AttendanceRecordRepository attendanceRecordRepository;
        private final ReportRepository reportRepository;
        private final AttendanceResponseMapper attendanceResponseMapper;
        private final DashboardResponseMapper dashboardResponseMapper;

        @Override
        public AdminDashboardResponse getAdminDashboard() {
                long totalAccounts = userRepository.count();
                long activeAccounts = userRepository.countByStatus(Status.ACTIVE);
                long inactiveAccounts = userRepository.countByStatus(Status.INACTIVE);
                long pendingAccounts = userRepository.countByStatus(Status.PENDING);
                var accountTypeDistribution = Arrays.stream(Role.values())
                                .map(role -> new AccountTypeDistributionResponse(
                                                role.name(),
                                                roleLabel(role),
                                                userRepository.countByRole(role)))
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
                long totalEmployees = userRepository.countByRoleNot(Role.ADMIN);
                LocalDate today = LocalDate.now();
                long presentToday = attendanceRecordRepository.countByAttendanceDateAndStatus(
                                today,
                                AttendanceStatus.PRESENT);
                long lateToday = attendanceRecordRepository.countByAttendanceDateAndStatus(
                                today,
                                AttendanceStatus.LATE);
                long attendedToday = presentToday + lateToday;
                long absentToday = Math.max(0, totalEmployees - attendedToday);
                Page<AttendanceResponse> attendanceHistory = attendanceRecordRepository
                                .findAllByOrderByAttendanceDateDesc(PageRequest.of(0, 10))
                                .map(attendanceResponseMapper::fromEntity);

                var monday = TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY).adjustInto(today);
                var weeklyStats = IntStream.rangeClosed(2, 6)
                                .mapToObj(dayOfWeek -> {
                                        LocalDate date = ((LocalDate) monday)
                                                        .plusDays(dayOfWeek - DayOfWeek.MONDAY.getValue());
                                        long present = attendanceRecordRepository.countByAttendanceDateAndStatus(date,
                                                        AttendanceStatus.PRESENT);
                                        long late = attendanceRecordRepository.countByAttendanceDateAndStatus(date,
                                                        AttendanceStatus.LATE);
                                        long attended = present + late;
                                        long absent = Math.max(0, totalEmployees - attended);
                                        return new DailyAttendanceStats(
                                                        "T" + (dayOfWeek - 1),
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
                User user = getCurrentUser();
                long totalReports = reportRepository.countByEmployee(user);
                long acceptedReports = reportRepository.countByEmployeeAndStatus(user, ReportStatus.ACCEPTED);
                long rejectedReports = reportRepository.countByEmployeeAndStatus(user, ReportStatus.REJECTED);
                long totalDays = attendanceRecordRepository.count();
                long attendedDays = attendanceRecordRepository.countByCheckInTimeNotNullAndCheckOutTimeNotNull();
                String attendenceRate = DashboardCalculator.showResultStr(attendedDays, totalDays);
                Page<AttendanceResponse> attendanceHistory = attendanceRecordRepository
                                .findByUser(user, PageRequest.of(0, 10))
                                .map(attendanceResponseMapper::fromEntity);
                return dashboardResponseMapper.toEmployeeDashboardResponse(
                                totalReports,
                                acceptedReports,
                                rejectedReports,
                                attendenceRate,
                                attendanceHistory);
        }

        private User getCurrentUser() {
                String username = SecurityUtil.getCurrentUserName();
                if (username == null) {
                        throw new IllegalArgumentException("Không tìm thấy người dùng");
                }
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        }

        private String roleLabel(Role role) {
                String roleName = role.name().toLowerCase(Locale.ROOT);
                return roleName.substring(0, 1).toUpperCase(Locale.ROOT) + roleName.substring(1);
        }
}
