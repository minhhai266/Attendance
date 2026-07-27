package com.attendenceSystem.module.dashboard.dto.response;

import java.util.List;
import com.attendenceSystem.module.dashboard.util.DashboardCalculator;

public record AdminDashboardResponse(
        long totalAccounts,
        long activeAccounts,
        long inactiveAccounts,
        long pendingAccounts,
        List<AccountTypeDistributionResponse> accountTypeDistribution
) {
    public String activeRate() {
        return DashboardCalculator.showResultStr(activeAccounts, totalAccounts);
    }

    public String inactiveRate() {
        return DashboardCalculator.showResultStr(inactiveAccounts, totalAccounts);
    }

    public String pendingRate() {
        return DashboardCalculator.showResultStr(pendingAccounts, totalAccounts);
    }
}
