package com.attendenceSystem.module.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;

public interface AttendanceService {

    AttendanceResponse checkIn();

    AttendanceResponse checkOut();

    Page<AttendanceResponse> getAttendanceHistory(Pageable pageable);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate);

    ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate, com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus status);

    List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate);

    List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate, com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus status);

    LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request);

    Page<LeaveRequestResponse> getLeaveRequests(Pageable pageable);

    Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable);

    LeaveDetailResponse getLeaveDetail(Long id);
}
