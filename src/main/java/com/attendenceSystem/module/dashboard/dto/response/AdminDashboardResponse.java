package com.attendenceSystem.module.dashboard.dto.response;

public record AdminDashboardResponse(
        long totalAccounts,
        long activeAccounts,
        long inactiveAccounts,
        long pendingAccounts
) {
}