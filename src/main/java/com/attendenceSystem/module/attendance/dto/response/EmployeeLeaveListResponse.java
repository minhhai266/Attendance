package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.Builder;

@Builder
public record EmployeeLeaveListResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        String status) {

}