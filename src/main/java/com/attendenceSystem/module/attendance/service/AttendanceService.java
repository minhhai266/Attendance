package com.attendenceSystem.module.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceHistoryStatsResponse;
import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerAttendanceListResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.entity.User;

public interface AttendanceService {

    Page<EmployeeAttendanceListResponse> getAttendanceHistory(Pageable pageable);

    Page<EmployeeAttendanceListResponse> getAttendanceHistory(LocalDate startDate, LocalDate endDate, AttendanceStatus status, Pageable pageable);

    AttendanceHistoryStatsResponse getAttendanceHistoryStats();

    AttendanceHistoryStatsResponse getAttendanceHistoryStats(LocalDate startDate, LocalDate endDate, AttendanceStatus status);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate,
            AttendanceStatus status);

    List<ManagerAttendanceListResponse> getManagerAttendanceList(LocalDate startDate, LocalDate endDate);

    List<ManagerAttendanceListResponse> getManagerAttendanceList(LocalDate startDate, LocalDate endDate, AttendanceStatus status);

    Optional<AttendanceRecord> getTodayAttendanceRecord(User user);
}
