package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.Builder;

@Builder
public record ManageLeaveListResponse(
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status) {

}