package com.attendenceSystem.module.attendance.dto.response;

import lombok.Builder;

@Builder
public record ManagerStatsResponse(
        long totalEmployees,
        long checkedIn,
        long checkedOut,
        long lateArrivals,
        long absent) {
}