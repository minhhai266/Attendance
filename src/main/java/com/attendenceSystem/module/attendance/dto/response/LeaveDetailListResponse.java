package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import lombok.Builder;

@Builder
public record LeaveDetailListResponse(
        Long id,
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status,
        String reason) {

}