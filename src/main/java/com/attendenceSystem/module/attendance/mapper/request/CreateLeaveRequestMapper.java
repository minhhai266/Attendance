package com.attendenceSystem.module.attendance.mapper.request;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CreateLeaveRequestMapper {
    public LeaveRequest toEntity(
            CreateLeaveRequest request,
            User user) {

        return LeaveRequest.builder()
                .user(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

    }
}
