package com.attendenceSystem.module.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;

public interface LeaveService {
    void acceptLeave(Long id);
    void rejectLeave(Long id);

    LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request);

    Page<LeaveRequestResponse> getLeaveRequests(Pageable pageable);

    Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable);

    LeaveDetailResponse getLeaveDetail(Long id);
}
