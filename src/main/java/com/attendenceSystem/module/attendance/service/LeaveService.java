package com.attendenceSystem.module.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.EmployeeLeaveListResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerLeaveListResponse;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

public interface LeaveService {
    void acceptLeave(Long id);
    void rejectLeave(Long id);

    LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request);

    Page<EmployeeLeaveListResponse> getLeaveRequests(Pageable pageable);

    Page<EmployeeLeaveListResponse> getLeaveRequests(LeaveStatus status, String week, Pageable pageable);

    Page<ManagerLeaveListResponse> getAllLeaveRequests(Pageable pageable);

    Page<ManagerLeaveListResponse> getAllLeaveRequests(String keyword, LeaveStatus status, String week, Pageable pageable);

    LeaveDetailResponse getLeaveDetail(Long id);
}
