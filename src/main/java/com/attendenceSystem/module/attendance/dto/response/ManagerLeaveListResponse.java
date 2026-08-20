package com.attendenceSystem.module.attendance.dto.response;

import java.time.LocalDate;


import lombok.Builder;

@Builder
public record ManagerLeaveListResponse(
        Long id,
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        String status) {

}