package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LeaveDetailResponseMapper {
    public LeaveDetailResponse fromEntity(LeaveRequest leave) {
        return new LeaveDetailResponse(
                leave.getId(),
                leave.getUser() == null ? null : leave.getUser().getUsername(),
                leave.getUser() == null ? null : leave.getUser().getFullName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus() != null ? leave.getStatus() : null,
                leave.getCreatedAt());
    }
}