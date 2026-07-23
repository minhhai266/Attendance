package com.attendenceSystem.module.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.entity.User;

public interface AttendanceService {

    AttendanceResponse checkIn();

    AttendanceResponse checkOut();

    AttendanceResponse checkIn(User user);

    AttendanceResponse checkOut(User user);

    Page<AttendanceResponse> getAttendanceHistory(Pageable pageable);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate, AttendanceStatus status);

    List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate);

    List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate, AttendanceStatus status);

    LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request);

    Page<LeaveRequestResponse> getLeaveRequests(Pageable pageable);

    Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable);

    LeaveDetailResponse getLeaveDetail(Long id);

    Optional<AttendanceRecord> getTodayAttendanceRecord(User user);
}
