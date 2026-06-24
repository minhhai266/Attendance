package com.attendenceSystem.module.dashboard.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DashboardCalculator {
    public double calculatePercent(long onTime, long total) {
        if (total <= 0) {
            return 0;
        }
        return (double) onTime / total * 100;
    }

    public String showResultStr(long onTime, long total) {
        return String.format("%.2f%%", calculatePercent(onTime, total));
    }
}
