package com.attendenceSystem.module.dashboard.dto.response;

import java.util.List;
import java.util.Locale;

public record AdminDashboardResponse(
        long totalAccounts,
        long activeAccounts,
        long inactiveAccounts,
        long pendingAccounts,
        List<AccountTypeDistributionResponse> accountTypeDistribution
) {
    public String activeRate() {
        return formatRate(activeAccounts);
    }

    public String inactiveRate() {
        return formatRate(inactiveAccounts);
    }

    public String pendingRate() {
        return formatRate(pendingAccounts);
    }

    private String formatRate(long value) {
        if (totalAccounts == 0) {
            return "0.0%";
        }
        return String.format(Locale.ROOT, "%.1f%%", value * 100.0 / totalAccounts);
    }
}
