package com.attendenceSystem.module.dashboard.dto.response;

public record AccountTypeDistributionResponse(
        String code,
        String label,
        long count) {
}
