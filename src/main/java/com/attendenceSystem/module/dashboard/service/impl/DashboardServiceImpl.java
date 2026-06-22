package com.attendenceSystem.module.dashboard.service.impl;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.EmployeeDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.service.DashboardService;
import com.attendenceSystem.module.report.entity.enums.ReportStatus;
import com.attendenceSystem.module.report.repository.ReportRepository;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;
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

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        long totalAccounts = userRepository.count();
        long activeAccounts = userRepository.countByIsActiveTrue();
        long inactiveAccounts = userRepository.countByIsActiveFalse();
        long pendingAccounts = userRepository.countByMustChangePasswordTrue();
        return new AdminDashboardResponse(totalAccounts, activeAccounts, inactiveAccounts, pendingAccounts);
    }

    @Override
    public ManagerDashboardResponse getManagerDashboard() {
        long totalEmployees = userRepository.countByRoleNot(Role.ADMIN);
        LocalDate today = LocalDate.now();
        long attendedEmployees = attendanceRecordRepository.countByAttendanceDate(today);
        long absentEmployees = Math.max(0, totalEmployees - attendedEmployees);
        Page<AttendanceResponse> attendanceHistory = attendanceRecordRepository
                .findAllByOrderByAttendanceDateDesc(PageRequest.of(0, 10))
                .map(attendanceResponseMapper::fromEntity);
        return new ManagerDashboardResponse(totalEmployees, attendedEmployees, absentEmployees, attendanceHistory);
    }

    @Override
    public EmployeeDashboardResponse getEmployeeDashboard() {
        User user = getCurrentUser();
        long totalReports = reportRepository.countByEmployee(user);
        long acceptedReports = reportRepository.countByEmployeeAndStatus(user, ReportStatus.ACCEPTED);
        long rejectedReports = reportRepository.countByEmployeeAndStatus(user, ReportStatus.REJECTED);
        Page<AttendanceResponse> attendanceHistory = attendanceRecordRepository
                .findByUser(user, PageRequest.of(0, 10))
                .map(attendanceResponseMapper::fromEntity);
        return new EmployeeDashboardResponse(totalReports, acceptedReports, rejectedReports, attendanceHistory);
    }

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUserName();
        if (username == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }
}
