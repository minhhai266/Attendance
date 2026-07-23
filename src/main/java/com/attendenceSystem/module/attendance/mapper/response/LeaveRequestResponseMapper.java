package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LeaveRequestResponseMapper {
    public LeaveRequestResponse fromEntity(LeaveRequest leave) {
        return new LeaveRequestResponse(
                leave.getId(),
                leave.getUser() == null ? null : leave.getUser().getFullName(),
                leave.getUser() == null || leave.getUser().getDepartment() == null
                        ? "N/A"
                        : leave.getUser().getDepartment().getDisplayName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus() != null ? leave.getStatus() : null,
                leave.getCreatedAt());
    }
}
