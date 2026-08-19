package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.Builder;

@Builder
public record EmployeeLeaveListResponse(
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status) {

}